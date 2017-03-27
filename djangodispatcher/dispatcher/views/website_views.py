from django.contrib.auth.decorators import login_required
from django.http import JsonResponse
from django.shortcuts import render, HttpResponseRedirect, HttpResponse
from django.contrib.auth import logout, authenticate, login
from pyfcm import FCMNotification
import openpyxl
import requests
import base64

from django.views.decorators.csrf import csrf_exempt

from dispatcher.forms import LoginForm
from dispatcher.models import Task, Profile, Pad, Skill, Location, Well
from django.contrib.auth.models import User

from django.utils import timezone
import math, json, pytz, datetime, decimal
from random import randint, uniform


@login_required
def index(request):
    user = Profile.objects.get(user=request.user)
    if user.admin:
        return render(request, 'screens/operator.html', {})
    else:
        return render(request, 'screens/dashboard.html', {})

# Map page

@login_required
def get_all_sensors(request):
    user = Profile.objects.get(user=request.user)
    return JsonResponse({"sensors": user.get_pads()})

# Map/Workers Page

@login_required
def get_all_workers(request):
    user = Profile.objects.get(user=request.user)
    return JsonResponse({"users": user.get_my_workers()})


@login_required
def get_current_user(request):
    user = Profile.objects.get(user=request.user)
    return JsonResponse(user.get_json())


@login_required
def get_possible_tasks(request):
    user = Profile.objects.get(user=request.user)
    my_tasks = []
    # for task in Task.objects.filter(worker=user, active=True).order_by("-date"):
    for task in user.task_set.all().order_by("-date"):
        my_tasks.append(task.get_json())
    return JsonResponse({'active_tasks': [user.current_task()]})

# TODO implement - change all actives to this in webapp
@login_required
def get_my_task(request):
    user = Profile.objects.get(user=request.user)
    return JsonResponse(user.current_task())

@login_required
def get_completed_user_tasks(request):
    user = Profile.objects.get(user=request.user)
    mytask = []
    for task in user.tasks.filter(active=False).order_by("-date"):
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
        for task in user.tasks.all():
            if task.active:
                result["active_tasks"].append(task.get_json())
            else:
                result["completed_tasks"].append(task.get_json())
        return JsonResponse(result)
    except Task.DoesNotExist:
        return JsonResponse({"result": "error"})

''''
{tag: { metric: skill needed},
data: {sensorID; sensorID,
       date: request date,
       waterLevel: water level,
       manuallyScheduled: was this automatic or not}}
'''
@csrf_exempt
def delegate(request):
    sample_data = request.body
    # TODO: take into account if a user is already at that location
    # TODO: python 3 receives 'bytes' instead of string, so data needs to be decoded
    # sample_data = request.body.decode('utf-8')
    body = json.loads(sample_data)
    try:
        tag = body['tag']
        data = body['data']
        if data["manuallyScheduled"]:
            manually_scheduled = True
        else:
            manually_scheduled = False
        print(manually_scheduled)
        sensorId = data["sensorID"]
        level_at_request = data["waterLevel"]
        try:
            date = data["date"]
        except KeyError:
            date = timezone.now()
        metric = tag["metric"]
        sensor = Pad.objects.get(sensorId=int(sensorId))
        skill = Skill.objects.get(title=metric)
    except Pad.DoesNotExist:
        return JsonResponse({"error": "No such sensor"})
    except Skill.DoesNotExist:
        return JsonResponse({"error": "No such job type"})
    except KeyError:
        return JsonResponse({"error": "Key Error"})
    correct_user = None
    smallest = -1
    # for all users that can solve the task
    task = Task.objects.create(pad=sensor, skill=skill, date=date, active=True, level_at_request=level_at_request, tank_capacity=100, manually_scheduled=manually_scheduled)
    # appropriate skill, not an admin, space in truck and exclude users with an active task
    for user in Profile.objects.filter(skills=skill, admin=False, truck_available_capacity__gte=level_at_request).exclude(tasks__active=True):
        location = user.current_location()
        distance = math.sqrt(math.pow(abs(sensor.location.lat-location.lat), 2) +
                             math.pow(abs(sensor.location.longitude-location.longitude), 2))
        if smallest == -1 or distance < smallest:
            correct_user = user
            smallest = distance
    if correct_user is None:
        return JsonResponse({"error": "no user found"})
    else:  # Add task and create notification
        correct_user.tasks.add(task)
        correct_user.push_notification(title="New Task", body=metric+" at Sensor "+sensorId)
    task.save()
    # return data
    try:
        user = Profile.objects.get(user=request.user)
        return JsonResponse({"users": user.get_my_workers(), "sensors": user.get_pads()})
    except Profile.DoesNotExist:
        return JsonResponse({"result": "success", 'name': task.skill.name, 'date': task.date, 'taskId': task.pk})


'''
This endpoint posts data to the delegate api to test functionality
'''
@csrf_exempt
def create_sample_task(request):
    date = datetime.datetime(2017, randint(1, 2), randint(1, 23), randint(1, 20), randint(1, 55), randint(1, 55), tzinfo=pytz.utc)
    r = requests.post("http://localhost:8000/api/delegate/", json={'tag': {'metric': 'WATER HAULING'},
                                              'data': {'sensorID': '857892', 'date': date, 'waterLevel': 79}})
    return JsonResponse({})



@login_required
def get_totals_data(request):
    active = Task.objects.filter(active=True).count()
    done = Task.objects.filter(active=False).count()
    num_users = Profile.objects.filter(admin=False).count()
    user = Profile.objects.get(user=request.user)
    return JsonResponse({"numActive": active, "numDone": done, "numUsers": num_users, "timeChart": user.monthly_time_spent(),
                         "waterHauled": user.monthly_volume_hauled(), "avgVolume": user.average_water_level_at_request(),
                         "manuallyScheduled": user.monthly_manually_scheduled()})


def logout_view(request):
    logout(request)
    return HttpResponseRedirect('/')


def login_view(request):
    if request.method == 'POST':
        form = LoginForm(request.POST)
        if form.is_valid():
            username = request.POST['username']
            password = request.POST['password']
            user = authenticate(username=username, password=password)
            if user is not None:
                if user.is_active:
                    login(request, user)
                    return HttpResponseRedirect('/')
                else:
                    return HttpResponse("disabled")

            else:
                return render(request, 'accounts/login.html', {'form': form})
    else:
        form = LoginForm()
    return render(request, 'accounts/login.html', {'form': form})


def init_2(request):
    wb = openpyxl.load_workbook('dispatcher/views/locations.xlsx')
    wells = []
    sheet = wb.get_sheet_by_name('Total Wells')
    # Lats start in G3, Longs in H3, id in L3 to 63039
    for i in range(33, 50):
        try:
            admin = User.objects.get(first_name=sheet['F'+str(i)].value)
            ad = Profile.objects.get(user=admin)
        except (User.DoesNotExist, Profile.DoesNotExist):
            admin = User.objects.create_superuser(username="admin", email="admin@glow.com", password="engineering",
                                                  first_name=sheet['F'+str(i)].value, last_name="")
            ad = Profile.objects.create(user=admin, profession="Operator", admin=True)
            location = Location.objects.create(lat=decimal.Decimal(sheet['G'+str(i)].value), longitude=decimal.Decimal(sheet['H'+str(i)].value))
            ad.locations.add(location)
            ad.save()

        location = Location.objects.create(lat=decimal.Decimal(sheet['G'+str(i)].value), longitude=decimal.Decimal(sheet['H'+str(i)].value))
        s = Pad.objects.create(sensorId=sheet['L'+str(i)].value, location=location, operator=ad)
        for j in range(0, int(sheet['I'+str(i)].value)):
            Well.objects.create(pad=s, water_capacity=100, water_level=100)

    skill = Skill.objects.create(title="WATER", name="Water Hauling")

    u1 = User.objects.create_user(username="wh1", email="wh1@glow.com", password="engineering", first_name="Water",
                                  last_name="Hauler")
    p1 = Profile.objects.create(user=u1, profession="Water Hauler", admin=False)
    p1.skills.add(skill)
    loc1 = Location.objects.create(lat=location.lat+decimal.Decimal(uniform(-1, 1)), longitude=location.longitude+decimal.Decimal(uniform(-1, 1)))
    p1.locations.add(loc1)
    p1.save()

    u2 = User.objects.create_user(username="wh2", email="wh2@glow.com", password="engineering", first_name="Water",
                                  last_name="Driver")
    p2 = Profile.objects.create(user=u2, profession="Water Hauler", admin=False)
    p2.skills.add(skill)
    loc1 = Location.objects.create(lat=location.lat+decimal.Decimal(uniform(-1, 1)), longitude=location.longitude+decimal.Decimal(uniform(-1, 1)))
    p2.locations.add(loc1)
    p2.save()

    u3 = User.objects.create_user(username="wh3", email="wh3@glow.com", password="engineering", first_name="Water",
                                  last_name="Filler")
    p3 = Profile.objects.create(user=u3, profession="Mechanic", admin=False)
    p3.skills.add(skill)
    loc1 = Location.objects.create(lat=location.lat+decimal.Decimal(uniform(-1, 1)), longitude=location.longitude+decimal.Decimal(uniform(-1, 1)))
    p3.locations.add(loc1)
    p3.save()

    # create past tasks
    skill = skill
    sensors = Pad.objects.all()
    users = Profile.objects.filter(admin=False)
    for i in range(0, 100):
        month = randint(1, 12)
        year = 2016
        if month < 3:
            year = 2017
        date = datetime.datetime(year, month, randint(1, 23), randint(1, 20), randint(1, 55), randint(1, 55), tzinfo=pytz.utc)
        hours = randint(1, 15)
        end_date = date+datetime.timedelta(hours=randint(hours+1, hours+10))+datetime.timedelta(minutes=randint(0,55))
        start_date = date+datetime.timedelta(hours=hours)
        level = randint(50, 100)
        manual = randint(0, 10) % 9 == 0
        t1 = Task.objects.create(pad=sensors[randint(0, 16)], skill=skill, date=date, start_date=start_date, manually_scheduled=manual,
                                 datecompleted=end_date, active=False, level_at_request=level, tank_capacity=100, amount_hauled=100-level)
        user = users[randint(0, 2)]
        user.tasks.add(t1)
        user.locations.add(t1.pad.location)
        user.save()
    # most recent location
    for user in users:
        loc1 = Location.objects.create(lat=location.lat+decimal.Decimal(uniform(-1, 1)), longitude=location.longitude+decimal.Decimal(uniform(-1, 1)))
        user.locations.add(loc1)
        user.save()
    # for i in range(0, 10):
    date = datetime.datetime(2017, datetime.date.today().month, datetime.date.today().day, randint(1, 20), randint(1, 55), randint(1, 55), tzinfo=pytz.utc)
    level = randint(50, 100)
    t1 = Task.objects.create(pad=sensors[randint(0, 16)], skill=skill, date=date, active=True, level_at_request=level,
                             tank_capacity=100)
    users[0].tasks.add(t1)
    users[0].save()
    # t1.possible_workers.add(users[0])
    print(users[0])
    #     t1.possible_workers.add(users[1])
    #     t1.possible_workers.add(users[2])
    #     t1.save()

    return JsonResponse({"wells": wells})
