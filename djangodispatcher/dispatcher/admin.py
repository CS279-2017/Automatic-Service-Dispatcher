from django.contrib import admin

# Register your models here.

from .models import Task, Location, Profile, Skill, Pad

admin.site.register(Task)
admin.site.register(Location)
admin.site.register(Profile)
admin.site.register(Skill)
admin.site.register(Pad)