from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile, Location, Skill

from django.utils import timezone
import datetime, urllib, base64


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

@csrf_exempt
def get_next_task(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()

        images = {}
        try:
            task = user.tasks.get(active=True, start_date__isnull=True)
        except Task.DoesNotExist:
            return JsonResponse({"result": "bad"}, status=401)

        intermediate = task.get_json()
        location = task.pad.location
        location = str(location.lat)+","+str(location.longitude)
        try:
            loc = images[location]
            intermediate["image"] = loc
        except KeyError:
            result = urllib.urlopen("https://maps.googleapis.com/maps/api/staticmap?center="+location+"&zoom=15&size=400x400&markers=color:red|label:C|"+location+"&maptype=roadmap&key=AIzaSyCIlABW-dOGWbwCJP6o-KwNzbJhx73H_7k").read()
            encoded_string = base64.b64encode(result)
            images[location] = encoded_string
            intermediate["image"] = encoded_string

        return JsonResponse(intermediate)
    except Profile.DoesNotExist:
        return JsonResponse({"result": "bad"}, status=401)


@csrf_exempt
def get_previous_tasks(request):
    session = request.POST.get('session', -1)
    deviceId = request.POST['deviceId']
    try:
        user = Profile.objects.get(session=session)
        user.device = deviceId
        user.save()
        mytask = []
        for task in user.tasks.filter(active=False).order_by("-date"):
            intermediate = task.get_json()
            mytask.append(intermediate)
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
def start_task(request):
    session = request.POST.get('session', -1)
    task_id = request.POST.get('taskId', -1)
    try:
        user = Profile.objects.get(session=session)
        task = Task.objects.get(pk=task_id)
        task.active = True
        task.start_date = datetime.datetime.now()
        user.tasks.add(task)
        # task.possible_workers.clear()
        task.start_date = datetime.datetime.today()
        task.save()
        user.save()
        return JsonResponse(task.get_json())
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
