# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.conf.urls import patterns, include, url

from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    url(r'^admin/', include(admin.site.urls)),
    url(r'^oauth2/', include('provider.oauth2.urls', namespace='oauth2')),
    url(r'^accounts/login/$', 'django.contrib.auth.views.login'),
    url(r'^users/', include('most.web.users.urls', namespace='users')), #urls of users app api
    url(r'^admin/', include(admin.site.urls)),
    url(r'^i18n/', include('django.conf.urls.i18n')),
    url(r'^demo/', 'most.main.views.examples', name='examples'),
    url(r'^test$', "most.web.authentication.views.test_auth"),

    # Task Group
    url(r'^task_group/new', 'most.main.views.task_group_new'),
    url(r'^task_group/is_provider', 'most.main.views.task_group_is_provider'),
    url(r'^task_group/set_provider', 'most.main.views.task_group_set_provider'),
    url(r'^task_group/set_active_state', 'most.main.views.task_group_set_active_state'),
    url(r'^task_group/add_user', 'most.main.views.task_group_add_user'),
    url(r'^task_group/remove_user', 'most.main.views.task_group_remove_user'),
    url(r'^task_group/list_users', 'most.main.views.task_group_list_users'),
    url(r'^task_group/add_related_task_group', 'most.main.views.task_group_add_related_task_group'),
    url(r'^task_group/remove_related_task_group', 'most.main.views.task_group_remove_related_task_group'),
    url(r'^task_group/list_related_task_group', 'most.main.views.task_group_list_related_task_group'),
    url(r'^task_group/has_clinicians', 'most.main.views.task_group_has_clinicians'),
    url(r'^task_group/list_clinicians', 'most.main.views.task_group_list_clinicians'),
    url(r'^task_group/has_clinician_provider', 'most.main.views.task_group_has_clinician_provider'),
    url(r'^task_group/list_clinician_providers', 'most.main.views.task_group_list_clinician_providers'),
    url(r'^task_group/search', 'most.main.views.task_group_search'),

    # Most User
    url(r'^most_user/login/', 'most.main.views.most_user_login'),
    url(r'^most_user/logout/', 'most.main.views.most_user_logout'),
    url(r'^most_user/new/', 'most.main.views.most_user_new'),
    url(r'^most_user/get_user_info/', 'most.main.views.most_user_get_user_info'),
    url(r'^most_user/search/', 'most.main.views.most_user_search'),
    url(r'^most_user/deactivate/', 'most.main.views.most_user_deactivate'),
    url(r'^most_user/activate/', 'most.main.views.most_user_activate'),

    # Clinician User
    url(r'^clinician_user/search/', 'most.main.views.clinician_user_search'),
    url(r'^clinician_user/new/', 'most.main.views.clinician_user_new'),
    url(r'^clinician_user/get_user_info/', 'most.main.views.clinician_user_get_user_info'),
    url(r'^clinician_user/is_provider/', 'most.main.views.clinician_user_is_provider'),
    url(r'^clinician_user/set_provider/', 'most.main.views.clinician_user_set_provider'),
)
