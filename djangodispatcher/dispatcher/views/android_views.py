from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile, Location, Job

from django.utils import timezone
import datetime


# TODO change name
@csrf_exempt
def get_my_task(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()
        return JsonResponse(user.current_task())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)

# TODO change name
@csrf_exempt
def get_possible_tasks(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()
        mytask = []
        # for task in Task.objects.filter(worker=user, active=True).order_by("-date"):
        for task in user.task_set.all().order_by("-date"):
            mytask.append(task.get_json())
        return JsonResponse({'active_tasks': mytask})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)


@csrf_exempt
def complete_task(request):
    session = request.POST.get('session', -1)
    task_id = request.POST.get('taskId', -1)
    try:
        user = Profile.objects.get(session=session)
        task = Task.objects.get(pk=task_id)
        task.active = False
        task.datecompleted = timezone.now()
        task.save()
        mytask = []
        for task in user.task_set.all().order_by("-date"):
            mytask.append(task.get_json())
        return JsonResponse({'active_tasks': mytask})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Invalid Session!"}, status=401)
    except Task.DoesNotExist:
        return JsonResponse({"result": "error"})


@csrf_exempt
def android_login(request):
    username = request.POST['username']
    password = request.POST['password']
    deviceId = request.POST['deviceId']
    print(deviceId)
    user = authenticate(username=username, password=password)
    if user is not None:
        login(request, user)
        # Redirect to a success page.
        print(request.session.session_key)
        profile = Profile.objects.get(user=user)
        profile.session = request.session.session_key
        profile.device = deviceId
        profile.save()
        return JsonResponse(profile.get_json())
    else:
        # Return an 'invalid login' error message.
        return JsonResponse({"result": "bad"}, status=401)


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


@csrf_exempt
def get_user(request):
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
        return JsonResponse(profile.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Error parsing inputs"}, status=401)

# todo: add ability to remove skills
@csrf_exempt
def update_user(request):
    session = request.POST.get('session', -1)
    first_name = request.POST.get('firstName', None)
    last_name = request.POST.get('lastName', None)
    email = request.POST.get('email', None)
    profession = request.POST.get('profession', None)
    jobs = request.POST.getlist('jobs', [])
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
        for j in profile.jobs.all():
            if j in jobs:
                pass
            else:
                profile.jobs.remove(j)
        # add everything in list
        for j in jobs:
            if not profile.jobs.filter(pk=j):  # and not Profile.profession.jobs.filter(pk=j):
                j = Job.objects.get(pk=j)
                profile.jobs.add(j)
        profile.save()
        return JsonResponse(profile.get_json())
    except Profile.DoesNotExist:
        return JsonResponse({"result": "Error parsing inputs"}, status=401)


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
