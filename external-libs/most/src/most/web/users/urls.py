# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.conf.urls import patterns, include, url
from most.web.users.views import clinician_user, task_group, most_user, demo
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'most.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),
    url(r'^demo/', demo.examples),
    url(r'^admin/', include(admin.site.urls)),
)

# MostUser API related urls
urlpatterns += patterns('',
    (r'^user/login/$', most_user.login_view),
    (r'^user/logout/$', most_user.logout_view),
    (r'^user/new/$', most_user.new),
    (r'^user/(?P<user_id>\d+)/get_user_info/$', most_user.get_user_info),
    (r'^user/search/$', most_user.search),
    (r'^user/(?P<user_id>\d+)/edit/$', most_user.edit),
    (r'^user/(?P<user_id>\d+)/deactivate/$', most_user.deactivate),
    (r'^user/(?P<user_id>\d+)/activate/$', most_user.activate),
)

# ClinicianUser API related urls
urlpatterns += patterns('',
    (r'^clinician_user/new/$', clinician_user.new),  # post
    #(r'^clinician_user/(?P<user_id>\d+)/edit/$', clinician_user.edit),  # post
    (r'^clinician_user/(?P<user_id>\d+)/is_provider/$', clinician_user.is_provider),  # get
    (r'^clinician_user/(?P<user_id>\d+)/set_provider/$', clinician_user.set_provider),  # post -> true | false
    (r'^clinician_user/search/$', clinician_user.search),  # get
    (r'^clinician_user/(?P<user_id>\d+)/get_user_info/$', clinician_user.get_user_info),  # get
)


# TaskGroup API related urls
urlpatterns += patterns('',
    (r'^task_group/search/$', task_group.search),
    (r'^task_group/new/$', task_group.new),
    (r'^task_group/(?P<task_group_id>\d+)/edit/$', task_group.edit),
    (r'^task_group/(?P<task_group_id>\d+)/get_task_group_info/$', task_group.get_task_group_info),
    (r'^task_group/list_available_states/$', task_group.list_available_states),
    (r'^task_group/(?P<task_group_id>\d+)/set_active_state/(?P<active_state>\w+)/$', task_group.set_active_state),
    (r'^task_group/(?P<task_group_id>\d+)/is_provider/$', task_group.is_provider),
    (r'^task_group/(?P<task_group_id>\d+)/set_provider/$', task_group.set_provider),
    (r'^task_group/(?P<task_group_id>\d+)/add_user/(?P<user_id>\d+)/$', task_group.add_user),
    (r'^task_group/(?P<task_group_id>\d+)/remove_user/(?P<user_id>\d+)/$', task_group.remove_user),
    (r'^task_group/(?P<task_group_id>\d+)/list_users/$', task_group.list_users),
    (r'^task_group/(?P<task_group_id>\d+)/add_related_task_group/(?P<related_task_group_id>\d+)/$',
     task_group.add_related_task_group),
    (r'^task_group/(?P<task_group_id>\d+)/remove_related_task_group/(?P<related_task_group_id>\d+)/$',
     task_group.remove_related_task_group),
    (r'^task_group/(?P<task_group_id>\d+)/list_related_task_groups/$', task_group.list_related_task_groups),
    (r'^task_group/(?P<task_group_id>\d+)/has_clinicians/$', task_group.has_clinicians),
    (r'^task_group/(?P<task_group_id>\d+)/list_clinicians/$', task_group.list_clinicians),
    (r'^task_group/(?P<task_group_id>\d+)/has_clinician_provider/$', task_group.has_clinician_provider),
    (r'^task_group/(?P<task_group_id>\d+)/list_clinician_providers/$', task_group.list_clinician_providers),
    #TODO add get_info method
)