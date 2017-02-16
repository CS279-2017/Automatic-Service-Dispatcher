from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile, Location

from django.utils import timezone
import datetime



@csrf_exempt
def get_active_user_tasks_android(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()
        mytask = []
        for task in Task.objects.filter(worker=user, active=True).order_by("-date"):
            mytask.append(task.get_json())
        return JsonResponse({'active_tasks': mytask})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)


@csrf_exempt
def complete_task(request):
    session = request.POST.get('session', -1)
    task_id = request.POST.get('taskId', -1)
    try:
        profile = Profile.objects.get(session=session)
        task = Task.objects.get(pk=task_id)
        task.active = False
        task.datecompleted = timezone.now()
        task.save()
        user = Profile.objects.get(user=request.user)
        result = {'completed_tasks': [], 'active_tasks': []}
        for task in Task.objects.filter(worker=user, active=False):
            if task.active:
                result["active_tasks"].append(task.get_json())
            else:
                result["completed_tasks"].append(task.get_json())
        return JsonResponse(result)
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
        return JsonResponse({'firstName': user.first_name, 'lastName': user.last_name, 'email': user.email,
                             'id': user.pk, 'profession': user.profile.profession.title,
                             "numActive": Task.objects.filter(worker=user.profile, active=True).count(),
                             "numDone": Task.objects.filter(worker=user.profile, active=False).count(),
                             "sessionId": request.session.session_key})
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
        user = profile.user
        return JsonResponse({'firstName': user.first_name, 'lastName': user.last_name, 'email': user.email,
                             'id': user.pk, 'profession': user.profile.profession.title,
                             "numActive": Task.objects.filter(worker=user.profile, active=True).count(),
                             "numDone": Task.objects.filter(worker=user.profile, active=False).count(),
                             "sessionId": session})
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
