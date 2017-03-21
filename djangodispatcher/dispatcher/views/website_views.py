from django.contrib.auth.decorators import login_required
from django.http import JsonResponse
from django.shortcuts import render, HttpResponseRedirect, HttpResponse
from django.contrib.auth import logout, authenticate, login
from pyfcm import FCMNotification
import openpyxl
import urllib
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
        sensorId = data["sensorID"]
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
    # sample is 0.313+0.1 for each queued (to account for distance)
    task = Task.objects.create(pad=sensor, skill=skill, date=date)
    for user in Profile.objects.filter(skills=skill, admin=False):
        location = user.current_location()
        distance = math.sqrt(math.pow(abs(sensor.location.lat-location.lat), 2) +
                             math.pow(abs(sensor.location.longitude-location.longitude), 2))
        value = distance + 0.1*user.num_active_tasks()
        if smallest == -1 or value < smallest:
            correct_user = user
            smallest = value
        task.possible_workers.add(user)
        user.push_notification(title="New Task", body=metric+" at Sensor "+sensorId)
    if correct_user is None:
        return JsonResponse({"error": "no user found"})
    # task = Task.objects.create(worker=correct_user, sensor=sensor, job=job, date=date)
    task.save()
    # Now push notification to user
    # correct_user.push_notification(title="New Task", body=metric+" at Sensor "+sensorId)
    return JsonResponse({"result": "success", # 'workerUsername': task.worker.user.username, 'workerId': task.worker.pk,
                         'name': task.skill.name, 'date': task.date, 'taskId': task.pk})


@csrf_exempt
def create_sample_task(request):
    # API_KEY = 'AAAA7bChu4E:APA91bE9IriEYJr7n6PV7I-lcZ8k82F2nYgI-GqkYUeC09g_XCN1yZvQq3iaziQQXM7Jbh4kMYyixnlZCgCOEXcdIPSfwLG4S7NKXkAxy-oYaMPK5BeioJOMy1SkxBp5rR5B7NwbCu9G'
    # push_service = FCMNotification(api_key=API_KEY)
    # # Your api-key can be gotten from:  https://console.firebase.google.com/project/<project-name>/settings/cloudmessaging
    # # AIzaSyComw1k-ukcSZcYgGRbae2MjOyka1PA60w
    # profile = Profile.objects.get(user__username="mechanic")
    # registration_id = profile.device
    # # registration_id = "fL0Crhz4i58:APA91bHXi1c8RE1s2SJ_7wL6ibrz0vWgyF4w1IaU5ZKy8QCbfh7YPOQBd8vzkRaH70fElhUpnXdjT_H-ANdZCRpbciQM3_FsLH_ZFxdBxDSg60ocwXkR5LIr_3gpqrdHTJjkQ8JwdkNs"
    # print len(registration_id)
    # message_title = "Uber update"
    # message_body = "Hi john, your customized news for today is ready"
    # result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
    # print result
    t1 = datetime.datetime.today()
    result = urllib.urlopen("https://maps.googleapis.com/maps/api/staticmap?center=40.718217,-73.998284&zoom=12&size=100x100&maptype=roadmap&key=AIzaSyCIlABW-dOGWbwCJP6o-KwNzbJhx73H_7k").read()
    print(datetime.datetime.today()-t1)
    encoded_string = base64.b64encode(result)
    print(encoded_string)
    return JsonResponse({})



@login_required
def get_totals_data(request):
    active = Task.objects.filter(active=True).count()
    done = Task.objects.filter(active=False).count()
    num_users = Profile.objects.filter(admin=False).count()
    user = Profile.objects.get(user=request.user)
    return JsonResponse({"numActive": active, "numDone": done, "numUsers": num_users, "timeChart": user.monthly_time_spent(),
                         "waterHauled": user.monthly_volume_hauled(), "avgVolume": user.average_water_level_at_request()})


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


def initialize(request):
    # run python manage.py flush from the command line before executing this
    # 37.4419
    loc1 = Location.objects.create(lat=37.455659, longitude=-122.194612) #lat=36.3, longitude=-86.5)
    loc2 = Location.objects.create(lat=37.6, longitude=-122)#lat=36.1, longitude=-87)
    loc3 = Location.objects.create(lat=37, longitude=-122.14)# lat=36.3, longitude=-86.9)
    loc4 = Location.objects.create(lat=37.8, longitude=-122.5)  #lat=35.9, longitude=-86.7)
    loc5 = Location.objects.create(lat=37.7, longitude=-121.6)  # lat=36.16270, longitude=-86.78160)  # electrician
    loc6 = Location.objects.create(lat=37.6, longitude=-121.5)  # lat=36, longitude=-86.75)  # mechanic
    loc7 = Location.objects.create(lat=38, longitude=-122)  # lat=36.3, longitude=-86.75)  # other mechanic

    j1 = Skill.objects.create(title="LOW_VOLTAGE", name="Low Voltage")
    j2 = Skill.objects.create(title="HIGH_VOLTAGE", name="High Voltage")
    j3 = Skill.objects.create(title="LOW_PRESSURE", name="Low Pressure")
    j4 = Skill.objects.create(title="HIGH_PRESSURE", name="High Pressure")
    j5 = Skill.objects.create(title="TEMPERATURE_CHANGE", name="Temperature Change")
    j6 = Skill.objects.create(title="HIGH_TEMPERATURE", name="High Temperature")

    s1 = Pad.objects.create(sensorId=1, location=loc1)
    s2 = Pad.objects.create(sensorId=2, location=loc2)
    s3 = Pad.objects.create(sensorId=3, location=loc3)
    s4 = Pad.objects.create(sensorId=4, location=loc4)

    admin = User.objects.create_superuser(username="admin", email="sam@gmail.com", password="engineering",
                                          first_name="CSX278", last_name="Class")
    ad = Profile.objects.create(user=admin, profession="Operator", admin=True)
    ad.skills.add(j1)
    ad.skills.add(j2)
    ad.skills.add(j3)
    ad.skills.add(j4)
    ad.skills.add(j5)
    ad.skills.add(j6)
    ad.locations.add(loc5)
    ad.save()
    u1 = User.objects.create_user(username="electrician", email="electrician@gmail.com", password="engineering", first_name="Joe",
                                  last_name="Electrician")
    p1 = Profile.objects.create(user=u1, profession="Electrician", admin=False)
    p1.skills.add(j1)
    p1.skills.add(j2)
    p1.locations.add(loc5)
    p1.save()
    u2 = User.objects.create_user(username="mechanic", email="mechanic@gmail.com", password="engineering", first_name="Ben",
                                  last_name="Mechanic")
    p2 = Profile.objects.create(user=u2, profession="Mechanic", admin=False)
    p2.skills.add(j3)
    p2.skills.add(j4)
    p2.skills.add(j5)
    p2.skills.add(j6)
    p2.locations.add(loc6)
    p2.save()
    u3 = User.objects.create_user(username="mechanic2", email="mechanic2@gmail.com", password="engineering", first_name="Other",
                                  last_name="Mechanic")
    p3 = Profile.objects.create(user=u3, profession="Mechanic", admin=False)
    p3.skills.add(j3)
    p3.skills.add(j4)
    p3.skills.add(j5)
    p3.skills.add(j6)
    p3.locations.add(loc7)
    p3.save()

    # create past tasks
    for i in range(0, 30):
        sensor = s4
        job = j4
        random = randint(1, 4)
        if random == 1:
            sensor = s1
        elif random == 2:
            sensor = s2
        elif random == 3:
            sensor = s3
        random = randint(1, 4)
        if random == 1:
            job = j1
        elif random == 2:
            job = j2
        elif random == 3:
            job = j3
        date = datetime.datetime(2017, randint(1, 2), randint(1, 23), randint(1, 20), randint(1, 55), randint(1,55), tzinfo=pytz.utc)
        hours = randint(1, 15)
        end_date = date+datetime.timedelta(hours=randint(hours+1, hours+10))+datetime.timedelta(minutes=randint(0,55))
        start_date = date+datetime.timedelta(hours=hours)
        t1 = Task.objects.create(pad=sensor, skill=job, date=date, start_date=start_date,
                                 datecompleted=end_date, active=False)
        if job == j1 or job == j2:
            p1.tasks.add(t1)
        else:
            if randint(0, 1) == 0:
                p2.tasks.add(t1)
            else:
                p3.tasks.add(t1)
    # create possible tasks
    for i in range(0, 30):
        sensor = s4
        job = j4
        random = randint(1, 4)
        if random == 1:
            sensor = s1
        elif random == 2:
            sensor = s2
        elif random == 3:
            sensor = s3
        random = randint(1, 4)
        if random == 1:
            job = j1
        elif random == 2:
            job = j2
        elif random == 3:
            job = j3
        date = datetime.datetime(2017, randint(1, 2), 23, randint(1, 20), randint(1, 55), randint(1,55), tzinfo=pytz.utc)
        t1 = Task.objects.create(pad=sensor, skill=job, date=date,
                                 datecompleted=date+datetime.timedelta(hours=randint(1, 15)), active=True)
        if job == j1 or job == j2:
            t1.possible_workers.add(p1)
        else:
            t1.possible_workers.add(p2)
            t1.possible_workers.add(p3)
    return JsonResponse({})


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
    for i in range(0, 30):
        date = datetime.datetime(2017, randint(1, 2), randint(1, 23), randint(1, 20), randint(1, 55), randint(1, 55), tzinfo=pytz.utc)
        hours = randint(1, 15)
        end_date = date+datetime.timedelta(hours=randint(hours+1, hours+10))+datetime.timedelta(minutes=randint(0,55))
        start_date = date+datetime.timedelta(hours=hours)
        level = randint(0, 50)
        t1 = Task.objects.create(pad=sensors[randint(0, 16)], skill=skill, date=date, start_date=start_date,
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
    for i in range(0, 10):
        date = datetime.datetime(2017, datetime.date.today().month, datetime.date.today().day, randint(1, 20), randint(1, 55), randint(1, 55), tzinfo=pytz.utc)
        level = randint(0, 50)
        t1 = Task.objects.create(pad=sensors[randint(0, 16)], skill=skill, date=date, active=True, level_at_request=level,
                                 tank_capacity=100)
        t1.possible_workers.add(users[0])
        t1.possible_workers.add(users[1])
        t1.possible_workers.add(users[2])
        t1.save()

    # create possible tasks
    # for i in range(0, 30):
    #     sensor = s4
    #     job = j4
    #     random = randint(1, 4)
    #     if random == 1:
    #         sensor = s1
    #     elif random == 2:
    #         sensor = s2
    #     elif random == 3:
    #         sensor = s3
    #     random = randint(1, 4)
    #     if random == 1:
    #         job = j1
    #     elif random == 2:
    #         job = j2
    #     elif random == 3:
    #         job = j3
    #     date = datetime.datetime(2017, randint(1, 2), 23, randint(1, 20), randint(1, 55), randint(1,55), tzinfo=pytz.utc)
    #     t1 = Task.objects.create(sensor=sensor, job=job, date=date,
    #                              datecompleted=date+datetime.timedelta(hours=randint(1, 15)), active=True)
    #     if job == j1 or job == j2:
    #         t1.possible_workers.add(p1)
    #     else:
    #         t1.possible_workers.add(p2)
    #         t1.possible_workers.add(p3)

    return JsonResponse({"wells": wells})
