from __future__ import unicode_literals
import uuid

from django.db import models
from django.contrib.auth.models import User
from django.contrib.sessions.models import Session

from django.utils import timezone

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

    def __unicode__(self):
        return str(self.lat)+" " +str(self.longitude)


class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    profession = models.ForeignKey(Profession)
    location = models.ForeignKey(Location)
    jobs = models.ManyToManyField("Job", blank=True)
    admin = models.BooleanField(default=False)
    #session = models.ForeignKey(Session, blank=True, null=True)
    session = models.CharField(max_length=32, default="0")
    device = models.CharField(default="0", max_length=200)

    def __unicode__(self):
        return self.user.username

    def num_active_tasks(self):
        return self.task_set.filter(active=True).count()



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


