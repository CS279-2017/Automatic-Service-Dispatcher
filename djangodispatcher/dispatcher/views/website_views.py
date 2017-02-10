from django.contrib.auth.decorators import login_required
from django.http import JsonResponse
from django.shortcuts import render, HttpResponseRedirect, HttpResponse
from django.contrib.auth import logout, authenticate, login
from pyfcm import FCMNotification


from django.views.decorators.csrf import csrf_exempt

from dispatcher.forms import LoginForm
from dispatcher.models import Task, Profile, Sensor, Job, Location, Profession
from django.contrib.auth.models import User

from django.utils import timezone
import math, json, pytz, datetime


@login_required
def index(request):
    user = Profile.objects.get(user=request.user)
    if user.admin:
        return render(request, 'screens/operator.html', {})
    else:
        return render(request, 'screens/dashboard.html', {})


@login_required
def get_current_user(request):
    user = request.user
    return JsonResponse({'username': user.username, 'firstName': user.first_name, 'lastName': user.last_name,
                         'email': user.email, 'id': user.pk})


@login_required
def get_active_user_tasks(request):
    user = Profile.objects.get(user=request.user)
    mytask = []
    for task in Task.objects.filter(worker=user, active=True).order_by("-date"):
        mytask.append(task.get_json())
    return JsonResponse({'active_tasks': mytask})


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
        sensor = Sensor.objects.get(sensorId=int(sensorId))
        job = Job.objects.get(title=metric)
    except Sensor.DoesNotExist:
        return JsonResponse({"error": "No such sensor"})
    except Job.DoesNotExist:
        return JsonResponse({"error": "No such job type"})
    except KeyError:
        return JsonResponse({"error": "Key Error"})
    correct_user = None
    smallest = -1
    # for all users that can solve the task
    # sample is 0.313+0.1 for each queued (to account for distance)
    for user in Profile.objects.filter(profession__jobs=job, admin=False):
        # queue closest
        distance = math.sqrt(math.pow(abs(sensor.location.lat-user.location.lat), 2) +
                             math.pow(abs(sensor.location.longitude-user.location.longitude), 2))
        value = distance + 0.1*user.num_active_tasks()
        if smallest == -1 or value < smallest:
            correct_user = user
            smallest = value
    if correct_user is None:
        return JsonResponse({"error": "no user found"})
    task = Task.objects.create(worker=correct_user, sensor=sensor, job=job, date=date)
    return JsonResponse({"result": "success", 'workerUsername': task.worker.user.username, 'workerId': task.worker.pk,
                         'name': task.job.name, 'date': task.date, 'taskId': task.pk})


@csrf_exempt
def create_sample_task(request):
    API_KEY = 'AAAA7bChu4E:APA91bE9IriEYJr7n6PV7I-lcZ8k82F2nYgI-GqkYUeC09g_XCN1yZvQq3iaziQQXM7Jbh4kMYyixnlZCgCOEXcdIPSfwLG4S7NKXkAxy-oYaMPK5BeioJOMy1SkxBp5rR5B7NwbCu9G'
    push_service = FCMNotification(api_key=API_KEY)
    # Your api-key can be gotten from:  https://console.firebase.google.com/project/<project-name>/settings/cloudmessaging
    # AIzaSyComw1k-ukcSZcYgGRbae2MjOyka1PA60w
    profile = Profile.objects.get(user__username="mechanic")
    registration_id = profile.device
    # registration_id = "fL0Crhz4i58:APA91bHXi1c8RE1s2SJ_7wL6ibrz0vWgyF4w1IaU5ZKy8QCbfh7YPOQBd8vzkRaH70fElhUpnXdjT_H-ANdZCRpbciQM3_FsLH_ZFxdBxDSg60ocwXkR5LIr_3gpqrdHTJjkQ8JwdkNs"
    print len(registration_id)
    message_title = "Uber update"
    message_body = "Hi john, your customized news for today is ready"
    result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)

    print result
    return JsonResponse({})

# operator APIs
@login_required
def get_all_workers(request):
    userlist = []
    for user in Profile.objects.filter(admin=False):
        tasklist = []
        for task in Task.objects.filter(worker=user, active=True).order_by("-date"):
            tasklist.append(task.get_json())
        userlist.append({'firstName': user.user.first_name, 'lastName': user.user.last_name, 'email': user.user.email,
                         'id': user.user.pk, 'profession': user.profession.title, 'activeTasks': tasklist,
                         "lat": user.location.lat, "long": user.location.longitude,
                         "numActive": Task.objects.filter(worker=user, active=True).count(),
                         "numDone": Task.objects.filter(worker=user, active=False).count()})
    return JsonResponse({"users": userlist})


@login_required
def get_all_sensors(request):
    sensor_list = []
    for sensor in Sensor.objects.all():
        sensor_list.append({"sensor": sensor.sensorId, "lat": sensor.location.lat, "long": sensor.location.longitude})
    return JsonResponse({"sensors": sensor_list})


@login_required
def get_totals_data(request):
    active = Task.objects.filter(active=True).count()
    done = Task.objects.filter(active=False).count()
    num_users = Profile.objects.filter(admin=False).count()
    titles = []
    sites = []
    titles.append("Site")
    for job in Job.objects.all().order_by("pk"):
        titles.append(job.name)
    titles.append("Total")

    for site in Sensor.objects.all().order_by("sensorId"):
        data = [site.sensorId]
        for job in Job.objects.all().order_by("pk"):
            data.append(Task.objects.filter(sensor=site, job=job).count())
        data.append(Task.objects.filter(sensor=site).count())
        sites.append(data)

    data = ["All"]
    for job in Job.objects.all().order_by("pk"):
            data.append(Task.objects.filter(job=job).count())
    data.append(Task.objects.all().count())
    sites.append(data)
    return JsonResponse({"numActive": active, "numDone": done, "numUsers": num_users, "sites":
                        {"titles": titles, "data": sites}})


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
    loc1 = Location.objects.create(lat=36.3, longitude=-86.5)
    loc2 = Location.objects.create(lat=36.1, longitude=-87)
    loc3 = Location.objects.create(lat=36.3, longitude=-86.9)
    loc4 = Location.objects.create(lat=35.9, longitude=-86.7)
    loc5 = Location.objects.create(lat=36.16270, longitude=-86.78160)  # electrician
    loc6 = Location.objects.create(lat=36, longitude=-86.75)  # mechanic
    loc7 = Location.objects.create(lat=36.3, longitude=-86.75)  # other mechanic

    j1 = Job.objects.create(title="LOW_VOLTAGE", name="Low Voltage")
    j2 = Job.objects.create(title="HIGH_VOLTAGE", name="High Voltage")
    j3 = Job.objects.create(title="LOW_PRESSURE", name="Low Pressure")
    j4 = Job.objects.create(title="HIGH_PRESSURE", name="High Pressure")
    j5 = Job.objects.create(title="TEMPERATURE_CHANGE", name="Temperature Change")
    j6 = Job.objects.create(title="HIGH_TEMPERATURE", name="High Temperature")

    op = Profession.objects.create(title="Operator")
    op.jobs.add(j1)
    op.jobs.add(j2)
    op.jobs.add(j3)
    op.jobs.add(j4)
    op.jobs.add(j5)
    op.jobs.add(j6)
    op.save()
    mech = Profession.objects.create(title="Mechanic")
    mech.jobs.add(j3)
    mech.jobs.add(j4)
    mech.jobs.add(j5)
    mech.jobs.add(j6)
    mech.save()
    elec = Profession.objects.create(title="Electrician")
    elec.jobs.add(j1)
    elec.jobs.add(j2)
    elec.save()

    s1 = Sensor.objects.create(sensorId=1, location=loc1)
    s2 = Sensor.objects.create(sensorId=2, location=loc2)
    s3 = Sensor.objects.create(sensorId=3, location=loc3)
    s4 = Sensor.objects.create(sensorId=4, location=loc4)

    admin = User.objects.create_superuser(username="admin", email="sam@gmail.com", password="engineering",
                                          first_name="CSX278", last_name="Class")
    Profile.objects.create(user=admin, profession=op, location=loc5, admin=True)
    u1 = User.objects.create_user(username="electrician", email="electrician", password="engineering", first_name="Joe",
                                  last_name="Electrician")
    p1 = Profile.objects.create(user=u1, profession=elec, location=loc5, admin=False)
    u2 = User.objects.create_user(username="mechanic", email="mechanic", password="engineering", first_name="Ben",
                                  last_name="Mechanic")
    p2 = Profile.objects.create(user=u2, profession=mech, location=loc6, admin=False)
    u3 = User.objects.create_user(username="mechanic2", email="mechanic2", password="engineering", first_name="Other",
                                  last_name="Mechanic")
    p3 = Profile.objects.create(user=u3, profession=mech, location=loc7, admin=False)

    Task.objects.create(worker=p1, sensor=s1, job=j1, date=datetime.datetime(2016, 11, 28, 14, 11, 56, tzinfo=pytz.utc),
                        datecompleted=datetime.datetime(2016, 11, 28, 18, 5, 48, tzinfo=pytz.utc), active=False)
    Task.objects.create(worker=p1, sensor=s2, job=j2, date=datetime.datetime(2016, 11, 28, 16, 48, 40, tzinfo=pytz.utc),
                        datecompleted=datetime.datetime(2016, 11, 28, 20, 7, 45, tzinfo=pytz.utc), active=False)
    Task.objects.create(worker=p1, sensor=s4, job=j2, date=datetime.datetime(2016, 12, 1, 20, 30, 40, tzinfo=pytz.utc),
                        datecompleted=datetime.datetime(2016, 12, 1, 20, 30, 40, tzinfo=pytz.utc), active=True)
    Task.objects.create(worker=p2, sensor=s3, job=j4, date=datetime.datetime(2016, 12, 1, 18, 11, 40, tzinfo=pytz.utc),
                        datecompleted=datetime.datetime(2016, 12, 1, 23, 46, 59, tzinfo=pytz.utc), active=False)
    Task.objects.create(worker=p2, sensor=s4, job=j3, date=datetime.datetime(2016, 12, 2, 12, 11, 40, tzinfo=pytz.utc),
                        datecompleted=datetime.datetime(2016, 12, 2, 12, 11, 40, tzinfo=pytz.utc), active=True)

    return JsonResponse({})