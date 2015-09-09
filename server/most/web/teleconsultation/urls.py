#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt
#.,

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
    url(r'rooms/$', "most.web.teleconsultation.views.get_rooms_for_taskgroup"),
    url(r'room/(?P<room_uuid>.*)/$', 'most.web.teleconsultation.views.get_room_by_uuid'),

    # teleconsultation
    url(r'(?P<teleconsultation_uuid>.*)/session/create/$', "most.web.teleconsultation.views.create_new_session"),
    #url(r'(?P<teleconsultation_uuid>.*)/close/$', "most.web.teleconsultation.views.close_teleconsultation"),
    url(r'create/$', "most.web.teleconsultation.views.create_teleconsultation"),
    url(r'today/open/$', "most.web.teleconsultation.views.get_open_teleconsultations"),
    url(r'today/$', "most.web.teleconsultation.views.get_teleconsultations"),
    url(r'session/(?P<session_uuid>.*)/start/$', "most.web.teleconsultation.views.start_session"),
    url(r'session/(?P<session_uuid>.*)/join/$', "most.web.teleconsultation.views.join_session"),
    url(r'session/(?P<session_uuid>.*)/run/$', "most.web.teleconsultation.views.run_session"),
    url(r'session/(?P<session_uuid>.*)/close/$', "most.web.teleconsultation.views.close_session"),
    url(r'session/(?P<session_uuid>.*)/$', "most.web.teleconsultation.views.get_session_data"),

    # url(r'sessions/waiting/$', "most.web.teleconsultation.views.get_open_teleconsultations"),

)
