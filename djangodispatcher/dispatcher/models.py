from __future__ import unicode_literals
import hashlib
from django.db.models import Avg, Sum
from django.db import models
from django.contrib.auth.models import User

from django.utils import timezone

from pyfcm import FCMNotification

import datetime


class Skill(models.Model):
    title = models.CharField(max_length=30, default="None")
    name = models.CharField(max_length=30, default="None")
    wage_rate = models.DecimalField(max_digits=8, decimal_places=2, default=50)

    def __unicode__(self):
        return self.title

    def json(self):
        return {'title': self.title, 'name': self.name}


class Location(models.Model):
    lat = models.DecimalField(max_digits=8, decimal_places=5)
    longitude = models.DecimalField(max_digits=8, decimal_places=5)
    time = models.DateTimeField(default=timezone.now())

    def __unicode__(self):
        return str(self.lat)+" " +str(self.longitude)

    def json(self):
        return {'lat': self.lat, 'longitude': self.longitude, 'time': self.time}


class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    profession = models.CharField(max_length=30, default="None")  # models.ForeignKey(Profession)
    locations = models.ManyToManyField(Location)
    skills = models.ManyToManyField("Skill", blank=True)
    tasks = models.ManyToManyField("Task", blank=True)
    truck_available_capacity = models.DecimalField(max_digits=8, decimal_places=2, default=100)

    admin = models.BooleanField(default=False)
    session = models.CharField(max_length=32, default="0")
    device = models.CharField(default="0", max_length=200)

    def __unicode__(self):
        return self.user.username

    def email_hash(self):
        return hashlib.md5(self.user.email.lower()).hexdigest()

    def current_task(self):
        task = self.tasks.filter(active=True)
        if not task:
            return None
        else:
            task = task[0]
            return task.get_json()

    def num_active_tasks(self):
        return self.task_set.filter(active=True).count()

    def current_location(self):
        recent = self.locations.order_by('-time')
        return recent[0]

    def push_notification(self, title, body):
        API_KEY = 'AAAA7bChu4E:APA91bE9IriEYJr7n6PV7I-lcZ8k82F2nYgI-GqkYUeC09g_XCN1yZvQq3iaziQQXM7Jbh4kMYyixnlZCgCOEXcdIPSfwLG4S7NKXkAxy-oYaMPK5BeioJOMy1SkxBp5rR5B7NwbCu9G'
        push_service = FCMNotification(api_key=API_KEY)
        registration_id = self.device
        push_service.notify_single_device(registration_id=registration_id, message_title=title, message_body=body)

    def get_json(self):
        jobs = []
        for j in self.skills.all():
            jobs.append({"name": j.name, "title": j.title, "id": j.pk})
        task = self.current_task()
        return {'firstName': self.user.first_name, 'lastName': self.user.last_name, 'email': self.user.email,
                'id': self.user.pk, 'profession': self.profession, 'emailHash': self.email_hash(),
                "numDone": self.tasks.filter(active=False).count(), "sessionId": self.session, "skills": jobs,
                'username': self.user.username, 'truckAvailableCapacity': self.truck_available_capacity,
                'activeTask': task, "lat": self.current_location().lat, "long": self.current_location().longitude,
                "numActive": self.tasks.filter(active=True).count()}

    # todo: change query
    def get_my_workers(self):
        workers = []
        for worker in Profile.objects.filter(tasks__pad__operator=self, admin=False).distinct():
            workers.append(worker.get_json())
        return workers

    def get_pads(self):
        pads = []
        for pad in self.pad_set.all():
            pads.append(pad.json())
        return pads

    # methods for massive data retireval
    def location_list(self):
        locations = []
        for loc in self.locations.all():
            locations.append({"lat": loc.lat, "longitude": loc.longitude, "time": loc.time, "serverKey": loc.pk})
        return locations

    def skill_list(self):
        skills = []
        for skill in self.skills.all():
            skills.append({"name": skill.name, "title": skill.title, "wageRate": skill.wage_rate, "serverKey": skill.pk})
        return skills

    def task_list(self):
        tasks = []
        for task in self.tasks.all():
            location = task.pad.location
            locations = [{"lat": location.lat, "longitude": location.longitude,
                          "time": location.time, "serverKey": location.pk}]
            pads = [{"sensorID": task.pad.sensorId, "serverKey": task.pad.pk,
                     "locationList": locations, "wellList": task.pad.get_wells()}]
            skills = [{"name": task.skill.name, "title": task.skill.title, "wageRate": task.skill.wage_rate, "serverKey": task.skill.pk}]
            tasks.append({"levelAtRequest": task.level_at_request, "tankCapacity": task.tank_capacity, "date": task.date,
                          "startDate": task.start_date, "dateCompleted": task.datecompleted, "active": task.active,
                          "amountHauled": task.amount_hauled, "serverKey": task.pk, "padList": pads, "skillList": skills})
        return tasks

    # data methods for operators
    def average_water_level_at_request(self):
        return Task.objects.filter(pad__operator=self).aggregate(Avg('level_at_request'))['level_at_request__avg']

    # not using
    def monthly_time_spent(self):
        months = []
        times = []
        for i in range(1, 13):
            months.append(datetime.date(1900, i, 1).strftime('%B'))
            cumulative = datetime.timedelta(hours=0)
            for task in Task.objects.filter(start_date__month=i):
                cumulative += (task.datecompleted-task.start_date)
            times.append(cumulative.days*24+cumulative.seconds/3600)
        return {"months": months, "times": times}

    def monthly_volume_hauled(self):
        months = []
        volumes = []
        for i in range(1, 13):
            months.append(datetime.date(1900, i, 1).strftime('%B'))
            cumulative = Task.objects.filter(start_date__month=i).aggregate(Sum('amount_hauled'))['amount_hauled__sum']
            if cumulative is None:
                volumes.append(0)
            else:
                volumes.append(cumulative)
        return {"months": months, "volume": volumes}

    def monthly_manually_scheduled(self):
        months = []
        manual = []
        not_manual = []
        for i in range(1, 13):
            months.append(datetime.date(1900, i, 1).strftime('%B'))
            manually_scheduled = Task.objects.filter(start_date__month=i, manually_scheduled=True).count()
            manual.append(manually_scheduled)
            not_manually_scheduled = Task.objects.filter(start_date__month=i, manually_scheduled=False).count()
            not_manual.append(not_manually_scheduled)
        return {"months": months, "manual": manual, "notManual": not_manual}

    def total_spent_on_water_hauling(self):
        months = []
        prices = []
        for i in range(1, 13):
            months.append(datetime.date(1900, i, 1).strftime('%B'))
            cumulative = 0
            for task in Task.objects.filter(start_date__month=i, active=False):
                cumulative += task.skill.wage_rate*task.amount_hauled
            prices.append(cumulative)
        return {"months": months, "cumulativeMoney": prices}


class Task(models.Model):
    declined_workers = models.ManyToManyField(Profile, blank=True)
    pad = models.ForeignKey("Pad", blank=True, null=True)
    skill = models.ForeignKey(Skill, blank=True, null=True)
    level_at_request = models.DecimalField(max_digits=8, decimal_places=2, default=0)
    tank_capacity = models.DecimalField(max_digits=8, decimal_places=2, default=0)
    manually_scheduled = models.BooleanField(default=False)

    date = models.DateTimeField(default=timezone.now())
    start_date = models.DateTimeField(null=True)
    datecompleted = models.DateTimeField(default=timezone.now())
    active = models.BooleanField(default=True)
    # at end of task use this
    amount_hauled = models.DecimalField(max_digits=8, decimal_places=2, default=0)

    def __unicode__(self):
        return self.skill.title+" "+self.pad.sensorId

    '''def get_json(self):
        return {'taskId': self.pk, 'workerId': self.worker.pk, 'name': self.job.name, 'date': self.date,
                "sensor": self.sensor.sensorId}'''

    def get_json(self):
        hoursOpen = -1
        minutes = -1
        if not self.active:
            time = (self.datecompleted-self.start_date)
            hoursOpen = time.days*24+time.seconds/3600
            minutes = time.seconds % 3600
            minutes /= 60
        return {'taskId': self.pk, 'name': self.skill.name, 'date': self.date,  'start_date': self.start_date, # 'workerId': self.worker.pk,
                "sensor": self.pad.sensorId, "dateCompleted": self.datecompleted, "hoursOpen": hoursOpen, "minutesOpen": minutes,
                "lattitude": self.pad.location.lat, "longitude": self.pad.location.longitude,
                "levelAtRequest": self.level_at_request, 'tankCapacity': self.tank_capacity,
                "estimatedPrice": self.level_at_request*self.skill.wage_rate}


class Pad(models.Model):
    sensorId = models.CharField(max_length=50, default="0", unique=True)
    passcode = models.CharField(max_length=10, default="0")
    location = models.ForeignKey(Location)
    operator = models.ForeignKey(Profile)
    water_capacity = models.DecimalField(max_digits=8, decimal_places=2, default=100)
    water_level = models.DecimalField(max_digits=8, decimal_places=2, default=0)

    def get_state(self):
        if self.task_set.filter(start_date__isnull=True, active=True):
            return "being_fixed"

    def get_wells(self):
        wells = []
        for well in self.well_set.all():
            wells.append(well.json())
        return wells

    def json(self):
        return {"sensor": self.sensorId, "lat": self.location.lat, "long": self.location.longitude, "locationId": self.location.pk,
                "state": self.get_state(), "wells": self.get_wells(), "waterCapacity": self.water_capacity,
                "waterLevel": self.water_level, "passcode": self.passcode}


class Well(models.Model):
    pad = models.ForeignKey(Pad)
    water_capacity = models.DecimalField(max_digits=8, decimal_places=2)
    water_level = models.DecimalField(max_digits=8, decimal_places=2)

    def json(self):
        return {"waterCapacity": self.water_capacity, "waterLevel": self.water_level, "serverKey": self.pk}
