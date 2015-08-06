#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt
#

from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'test/$',  "most.web.teleconsultation.views.test"),

    # task groups
    url(r'taskgroups/(?P<device_uuid>.*)/$', "most.web.teleconsultation.views.get_task_groups_for_device"),

    # get operators
    url(r'applicants/(?P<taskgroup_uuid>.*)/$', "most.web.teleconsultation.views.get_applicants_for_taskgroups"),

    # get rooms
    url(r'rooms/(?P<taskgroup_uuid>.*)/$', "most.web.teleconsultation.views.get_rooms_for_taskgroup"),
    url(r'room/(?P<room_uuid>.*)/$', 'most.web.teleconsultation.views.get_room_by_uuid'),

    # teleconsultation
    url(r'create/$', "most.web.teleconsultation.views.create_teleconsultation"),
    url(r'list/$', "most.web.teleconsultation.views.get_teleconsultations"),
    url(r'session/start/$', "most.web.teleconsultation.views.start_session"),
    url(r'session/$', "most.web.teleconsultation.views.get_session_data"),
    url(r'session/run/$', "most.web.teleconsultation.views.run_session"),
    url(r'session/join/$', "most.web.teleconsultation.views.join_session"),

    url(r'sessions/active/$', "most.web.teleconsultation.views.get_active_sessions"),

)
