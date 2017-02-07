from django.contrib.auth.decorators import login_required
from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile

from django.utils import timezone



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


@login_required
def get_completed_user_tasks(request):
    user = Profile.objects.get(user=request.user)
    mytask = []
    for task in Task.objects.filter(worker=user, active=False).order_by("-date"):
        mytask.append(task.get_json())
    return JsonResponse({'completed_tasks': mytask})


@login_required
def complete_task(request):
    task_id = request.POST.get('taskId', -1)
    try:
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
    except Task.DoesNotExist:
        return JsonResponse({"result": "error"})


@csrf_exempt
def android_login(request):
    #session = Session.objects.get(session_key="sosa8nrhvf0dwvjybw10davhyu2akr03")
    #uid = session.get_decoded().get('_auth_user_id')
    #user = User.objects.get(pk=uid)
    #print(repr(session))
    #session.delete()
    #print(user.first_name)
    # print(_get_user_session_key(request))
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
