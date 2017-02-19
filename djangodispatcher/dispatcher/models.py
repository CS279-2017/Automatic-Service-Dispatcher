from __future__ import unicode_literals
import uuid

from django.db import models
from django.contrib.auth.models import User

from django.utils import timezone

from pyfcm import FCMNotification

class Profession(models.Model):
    title = models.CharField(max_length=30, default="None")
    jobs = models.ManyToManyField("Job", blank=True)

    def __unicode__(self):
        return self.title


class Job(models.Model):
    title = models.CharField(max_length=30, default="None")
    name = models.CharField(max_length=30, default="None")

    def __unicode__(self):
        return self.title


class Location(models.Model):
    lat = models.DecimalField(max_digits=8, decimal_places=5)
    longitude = models.DecimalField(max_digits=8, decimal_places=5)
    time = models.DateTimeField(default=timezone.now())

    def __unicode__(self):
        return str(self.lat)+" " +str(self.longitude)


class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    profession = models.ForeignKey(Profession)
    locations = models.ManyToManyField(Location)
    jobs = models.ManyToManyField("Job", blank=True)
    admin = models.BooleanField(default=False)
    session = models.CharField(max_length=32, default="0")
    device = models.CharField(default="0", max_length=200)

    def __unicode__(self):
        return self.user.username

    def num_active_tasks(self):
        return self.task_set.filter(active=True).count()

    def current_location(self):
        recent = self.locations.order_by('time')
        return recent[0]

    def push_notification(self, title, body):
        API_KEY = 'AAAA7bChu4E:APA91bE9IriEYJr7n6PV7I-lcZ8k82F2nYgI-GqkYUeC09g_XCN1yZvQq3iaziQQXM7Jbh4kMYyixnlZCgCOEXcdIPSfwLG4S7NKXkAxy-oYaMPK5BeioJOMy1SkxBp5rR5B7NwbCu9G'
        push_service = FCMNotification(api_key=API_KEY)
        registration_id = self.device
        push_service.notify_single_device(registration_id=registration_id, message_title=title, message_body=body)

    def get_json(self):
        jobs = []
        all_jobs = []
        for j in self.jobs.all():
            jobs.append({"name": j.name, "title": j.title, "id": j.pk})
        for p in self.profession.jobs.all():
            jobs.append({"name": p.name, "title": p.title, "id": p.pk})
        for j in Job.objects.all():
            all_jobs.append({"name": j.name, "title": j.title, "id": j.pk})
        return {'firstName': self.user.first_name, 'lastName': self.user.last_name, 'email': self.user.email,
                'id': self.user.pk, 'profession': self.profession.title,
                "numActive": Task.objects.filter(worker=self, active=True).count(),
                "numDone": Task.objects.filter(worker=self, active=False).count(),
                "sessionId": self.session, "skills": jobs, "possibleSkills": all_jobs}


class Task(models.Model):
    worker = models.ForeignKey(Profile)
    sensor = models.ForeignKey("Sensor", blank=True, null=True)
    job = models.ForeignKey(Job, blank=True, null=True)
    date = models.DateTimeField(default=timezone.now())
    datecompleted = models.DateTimeField(default=timezone.now())
    active = models.BooleanField(default=True)

    def __unicode__(self):
        return self.job.title

    '''def get_json(self):
        return {'taskId': self.pk, 'workerId': self.worker.pk, 'name': self.job.name, 'date': self.date,
                "sensor": self.sensor.sensorId}'''

    def get_json(self):
        hoursOpen = -1
        if not self.active:
            time = (self.datecompleted-self.date)
            hoursOpen = time.days*24+time.seconds/3600
        return {'taskId': self.pk, 'workerId': self.worker.pk, 'name': self.job.name, 'date': self.date,
                "sensor": self.sensor.sensorId, "dateCompleted": self.datecompleted, "hoursOpen": hoursOpen}

class Sensor(models.Model):
    sensorId = models.CharField(max_length=50, default="0", unique=True)
    location = models.ForeignKey(Location)


