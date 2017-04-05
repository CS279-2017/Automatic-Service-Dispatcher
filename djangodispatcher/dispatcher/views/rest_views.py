from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile, Location, Skill

from django.utils import timezone
import datetime, urllib, base64


@csrf_exempt
def retrieve_all_data(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST.get('deviceId', -1)
    try:
        profile = Profile.objects.get(session=session)
        profile.device = deviceId
        profile.save()
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)

    results = {"profession": profile.profession, "admin": profile.admin, "session": profile.session,
               "device": profile.device, "serverKey": profile.pk, "locationList": profile.location_list(),
               "skillList": profile.skill_list(), "taskList": profile.task_list()}
    return JsonResponse(results)

'''
Method for skills
Params:
"session": validates which user is making the call

Post:
All skills associated with a user returned
'''
@csrf_exempt
def skill(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)

    skills = []
    for skill in profile.skills.all():
        skills.append(skill.json())
    return JsonResponse({"skills": skills})

'''
Method for locations
Params:
"session": validates which user is making the call
"method": create or retrieve
"latitude": new lat
"longitude": new long
"timestamp": the time of the new location

Post:
All locations associated with the passed in profile are returned and any new location is added
'''
@csrf_exempt
def location(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)
    # update location
    if request.POST.get('method', 'retrieve') == 'create':  # create task
        try:
            lat = request.POST.get('latitude', None)
            longitude = request.POST.get('longitude', None)
            time = datetime.datetime.fromtimestamp(float(request.POST.get('timestamp', None)))
            if not lat or not longitude:
                return JsonResponse({"result": "Unexpected Input"}, status=401)
            loc = Location.objects.create(lat=lat, longitude=longitude, time=time)
            profile.locations.add(loc)
            profile.save()
        except (ValueError, TypeError):
            return JsonResponse({"result": "Error parsing inputs"}, status=401)

    locations = []
    for loc in profile.locations.all():
        locations.append(loc.json())
    return JsonResponse({"locations": locations})

'''
Method for profile
Params:
"session": validates which user is making the call

Post:
Profile information returned
'''
@csrf_exempt
def profile(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)

    return JsonResponse({"profile": profile.get_json()})

'''
Method for tasks
Params:
"session": validates which user is making the call
"method": decline, accept or complete --> if parameter is something else, then just all tasks are retrieved
"taskId": the task that should be modified

Post:
The proper task has been updated and all user tasks are returned
'''
@csrf_exempt
def tasks(request):
    # session needed for updating and retrieving
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)

    # Retrieve all user tasks
    if request.POST.get('method', 'retrieve') == 'decline':  # decline task
        task_id = request.POST.get('taskId', -1)
        try:
            task = Task.objects.get(pk=task_id)
            task.start_date = None
            profile.tasks.remove(task)
            task.declined_workers.add(profile)
            task.save()
            profile.save()
            # TODO: now redelegate
        except Task.DoesNotExist:
            return JsonResponse({"Error": "No such task to decline"}, status=401)
    elif request.POST.get('method', 'retrieve') == 'accept':  # accept task
        task_id = request.POST.get('taskId', -1)
        try:
            task = Task.objects.get(pk=task_id)
            task.start_date = datetime.datetime.today()
            task.save()
        except Task.DoesNotExist:
            return JsonResponse({"Error": "No such task to accept"}, status=401)
    elif request.POST.get('method', 'retrieve') == 'complete':  # complete task
        task_id = request.POST.get('taskId', -1)
        try:
            task = Task.objects.get(pk=task_id)
            task.active = False
            task.datecompleted = timezone.now()
            task.save()
        except Task.DoesNotExist:
            return JsonResponse({"Error": "No such task to complete"}, status=401)

    # Get all tasks after method
    tasks = []
    for task in profile.tasks.all():
        tasks.append(task.get_json())
    return JsonResponse({"tasks": tasks})


'''
Method for pads
Params:
"session": validates which user is making the call

Post:
Pad information for the one current task is retrieved
'''
@csrf_exempt
def pad(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)

    pad_result = {"pads": None}
    task = profile.tasks.filter(active=True)
    if task:
        task = task[0]
        pad_result = {"pads": task.pad.json()}
    return JsonResponse(pad_result)


'''
Method for wells
Params:
"session": validates which user is making the call

Post:
Pad information for the one current task is retrieved
'''
@csrf_exempt
def well(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Invalid Session"}, status=401)

    well_result = {"wells": None}
    task = profile.tasks.filter(active=True)
    if task:
        task = task[0]
        well_result = {"wells": task.pad.get_wells()}
    return JsonResponse(well_result)
