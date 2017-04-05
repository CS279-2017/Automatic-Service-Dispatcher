from django.conf.urls import url
from .views import website_views, android_views, rest_views

urlpatterns = [
    url(r'^$', website_views.index, name='index'),

    url(r'^api/currentuser', website_views.get_current_user, name='get_user'),
    url(r'^api/possible_tasks', website_views.get_possible_tasks, name='possible'), # todo change in web
    url(r'^api/my_task', website_views.get_my_task, name='my task'), # todo change in web
    url(r'^api/completedtasks', website_views.get_completed_user_tasks, name='completed_tasks'),
    url(r'^api/finishtask', website_views.complete_task, name='complete_task'),

    url(r'^api/delegate', website_views.delegate, name='delegate'),

    url(r'^api/allusers', website_views.get_all_workers, name='all_workers'),
    url(r'^api/sensors', website_views.get_all_sensors, name='all_sensors'),
    url(r'^api/totaldata', website_views.get_totals_data, name='all_data'),

    url(r'^init', website_views.init_2, name='init2'),

    url(r'^accounts/logout/$', website_views.logout_view, name='logout'),
    url(r'^accounts/login/$', website_views.login_view, name='login'),

    # android
    url(r'^api/android/next_task', android_views.get_next_task, name='possible_tasks_android'), #todo change in android
    url(r'^api/android/previous_tasks', android_views.get_previous_tasks, name='previous_tasks_android'), #todo change in android
    url(r'^api/android/my_task', android_views.get_my_task, name='my_task_android'), #todo change in android
    url(r'^api/android/update_location', android_views.update_location, name='android_location_updater'),
    url(r'^api/android/start_task', android_views.start_task, name='android_start_task'),
    url(r'^api/android/complete_task', android_views.complete_task, name='android_complete_task'),
    url(r'^api/android/get_user', android_views.get_user, name='get_user'),
    url(r'^api/android/update_user', android_views.update_user, name='update_user'),

    url(r'^accounts/android_login/$', android_views.android_login, name='android login'),
    url(r'^accounts/check_session/$', android_views.check_session, name='check_session'),
    url(r'^accounts/android_logout/$', android_views.android_logout, name='android logout'),


    url(r'^api/create_task/$', website_views.create_sample_task, name='create task'),


    url(r'^api/android/all_data/$', rest_views.retrieve_all_data, name='get_all_data'),
]