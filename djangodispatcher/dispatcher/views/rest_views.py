from django.http import JsonResponse
from django.contrib.auth import authenticate, login

from django.views.decorators.csrf import csrf_exempt

from dispatcher.models import Task, Profile, Location, Skill

from django.utils import timezone
import datetime, urllib, base64

@csrf_exempt
def tasks(request):
    # session needed for updating and retrieving
    session = request.POST.get('session', -1)
    try:
        profile = Profile.objects.get(session=session)
    except Profile.DoexNotExist:
        return JsonResponse({"result": "Error parsing inputs"}, status=401)

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