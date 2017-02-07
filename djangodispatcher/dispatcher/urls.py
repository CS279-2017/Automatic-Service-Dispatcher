from django.conf.urls import url
from .views import website_views, android_views

urlpatterns = [
    url(r'^$', website_views.index, name='index'),

    url(r'^api/currentuser', website_views.get_current_user, name='get_user'),
    url(r'^api/activetasks', website_views.get_active_user_tasks, name='active_tasks'),
    url(r'^api/completedtasks', website_views.get_completed_user_tasks, name='completed_tasks'),
    url(r'^api/finishtask', website_views.complete_task, name='complete_task'),

    url(r'^api/delegate', website_views.delegate, name='delegate'),

    url(r'^api/allusers', website_views.get_all_workers, name='all_workers'),
    url(r'^api/sensors', website_views.get_all_sensors, name='all_sensors'),
    url(r'^api/totaldata', website_views.get_totals_data, name='all_data'),

    url(r'^initialize', website_views.initialize, name='init'),

    url(r'^accounts/logout/$', website_views.logout_view, name='logout'),
    url(r'^accounts/login/$', website_views.login_view, name='login'),

    # android
    url(r'^api/android/activetasks', android_views.get_active_user_tasks_android, name='active_tasks_android'),
    url(r'^accounts/android_login/$', android_views.android_login, name='android login'),
    url(r'^accounts/check_session/$', android_views.check_session, name='check_session'),
    url(r'^accounts/android_logout/$', android_views.android_logout, name='android logout'),
]