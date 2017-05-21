from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile, Location, Skill

from django.utils import timezone
import datetime, urllib, base64

from .website_views import redelegate


# This method queries for any current tasks the user is working on
@csrf_exempt
def get_my_task(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:  # check for logged in user
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()
        try:  # get task that is active and has been started
            task = user.tasks.get(active=True, start_date__isnull=False)
        except Task.DoesNotExist:
            return JsonResponse({"result": "bad"}, status=401)
        return JsonResponse(task.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)

# this method queries for any tasks delegated to the user that they have not accepted yet
@csrf_exempt
def get_next_task(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:  # checked for logged in user
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()

        images = {}
        try:  # get task that is active but has not been started
            task = user.tasks.get(active=True, start_date__isnull=True)
        except Task.DoesNotExist:
            return JsonResponse({"result": "bad"}, status=401)

        intermediate = task.get_json()
        location = task.pad.location  # get thumbnail view of pad location and add to task json
        location = str(location.lat)+","+str(location.longitude)
        try:
            loc = images[location]
            intermediate["image"] = loc
        except KeyError:
            result = urllib.urlopen("https://maps.googleapis.com/maps/api/staticmap?center="+location+"&zoom=13&size=1500x1500&markers=color:red|label:C|"+location+"&maptype=roadmap&key=AIzaSyCIlABW-dOGWbwCJP6o-KwNzbJhx73H_7k").read()
            encoded_string = base64.b64encode(result)
            images[location] = encoded_string
            intermediate["image"] = encoded_string

        return JsonResponse(intermediate)
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)


# reject task delegated to user
@csrf_exempt
def cancel_current_task(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    taskId = request.POST.get('taskId')
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        task = Task.objects.get(pk=int(taskId))
        task.start_date = None
        task.declined_workers.add(user)  # add to list of declined workers so they don't recieve it again
        user.tasks.remove(task) # remove task from their queue
        task.save()
        user.save()  # redelegate task
        redelegate({'tag': {'metric': task.skill.title}, 'data': {'sensorID': task.pad.sensorId, 'date': task.date,
        'waterLevel': task.level_at_request, 'manuallyScheduled': task.manually_scheduled}}, user.user)
        return JsonResponse({})
    except (Profile.DoesNotExist, Task.DoesNotExist, ValueError):
        return JsonResponse({"result": "bad"}, status=401)


# get tasks the user has completed
@csrf_exempt
def get_previous_tasks(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()
        mytask = [] # iterate through tasks and get json
        for task in user.tasks.filter(active=False).order_by("-date"):
            intermediate = task.get_json()
            mytask.append(intermediate)
        return JsonResponse({'active_tasks': mytask})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)


# finish a task
@csrf_exempt
def complete_task(request):
    session = request.POST.get('session', -1)
    task_id = request.POST.get('taskId', -1)
    try:
        user = Profile.objects.get(session=session)
        task = Task.objects.get(pk=task_id)
        task.active = False
        task.datecompleted = timezone.now()
        # emptied tank
        task.pad.water_level = 0
        task.pad.save()
        # record amount hauled
        task.amount_hauled = task.level_at_request
        task.save()
        mytask = []
        for task in user.task_set.all().order_by("-date"):
            mytask.append(task.get_json())
        return JsonResponse({'active_tasks': mytask})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Invalid Session!"}, status=401)
    except Task.DoesNotExist:
        return JsonResponse({"result": "error"})


# user accepts a task that has been delegated to them
@csrf_exempt
def start_task(request):
    session = request.POST.get('session', -1)
    task_id = request.POST.get('taskId', -1)
    try:
        user = Profile.objects.get(session=session)
        task = Task.objects.get(pk=task_id)
        # update start time
        task.start_date = datetime.datetime.today()
        task.save()
        user.save()
        return JsonResponse(task.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Invalid Session!"}, status=401)
    except Task.DoesNotExist:
        return JsonResponse({"result": "error"})


# login for android
@csrf_exempt
def android_login(request):
    username = request.POST['username']
    password = request.POST['password']
    deviceId = request.POST['deviceId']

    user = authenticate(username=username, password=password)
    if user is not None:
        login(request, user)
        # Redirect to a success page.
        profile = Profile.objects.get(user=user)
        profile.session = request.session.session_key
        profile.device = deviceId
        profile.save()
        return JsonResponse(profile.get_json())
    else:
        # Return an 'invalid login' error message.
        return JsonResponse({"result": "bad"}, status=401)


# check session from android to see if they are already logged in
@csrf_exempt
def check_session(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        profile = Profile.objects.get(session=session)
        profile.device = deviceId
        profile.save()
        return JsonResponse(profile.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)


# update the users current location
@csrf_exempt
def update_location(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
        lat = request.POST.get('latitude', None)
        longitude = request.POST.get('longitude', None)
        time = datetime.datetime.fromtimestamp(float(request.POST.get('timestamp', None)))
        if not lat or not longitude:
            return JsonResponse({"result": "Unexpected Input"}, status=401)
        loc = Location.objects.create(lat=lat, longitude=longitude, time=time)
        profile.locations.add(loc)
        profile.save()
    except (Profile.DoexNotExist, ValueError, TypeError):
        return JsonResponse({"result": "Error parsing inputs"}, status=401)


# get the logged in user's information
@csrf_exempt
def get_user(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
        return JsonResponse(profile.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Error parsing inputs"}, status=401)

# user can update their info (not currently supported)
@csrf_exempt
def update_user(request):
    session = request.POST.get('session', -1)
    first_name = request.POST.get('firstName', None)
    last_name = request.POST.get('lastName', None)
    email = request.POST.get('email', None)
    profession = request.POST.get('profession', None)
    skills = request.POST.getlist('skills', [])
    try:
        profile = Profile.objects.get(session=session)
        user = profile.user
        user.first_name = first_name
        user.last_name = last_name
        user.email = email
        user.save()
        profile.profession = profession
        profile.user = user
        # remove ones not in list
        for j in profile.skills.all():
            if j in skills:
                pass
            else:
                profile.skills.remove(j)
        # add everything in list
        for j in skills:
            if not profile.skills.filter(pk=j):  # and not Profile.profession.jobs.filter(pk=j):
                j = Skill.objects.get(pk=j)
                profile.skills.add(j)
        profile.save()
        return JsonResponse(profile.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Error parsing inputs"}, status=401)

# android logout
@csrf_exempt
def android_logout(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
        profile.session = "0"
        profile.device = "0"
        profile.save()
        return JsonResponse({})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)
