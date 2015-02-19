# -*- coding: utf-8 -*-

# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.conf.urls import patterns, include, url
from most.web.exams.views import section, exam, visit, demo
from django.contrib import admin
admin.autodiscover()


urlpatterns = patterns('',
    # Examples:
    url(r'^demo/', demo.examples),
    url(r'^admin/', include(admin.site.urls)),
)


# Section API related urls
urlpatterns += patterns('',
    (r'^section/new/$', section.new),
    (r'^section/(?P<section_id>\d+)/edit/$', section.edit),
    (r'^section/(?P<section_id>\d+)/delete/$', section.delete),
    (r'^section/search/$', section.search),
    (r'^section/(?P<section_id>\d+)/get_info/$', section.get_info),
)


# Exam API related urls
urlpatterns += patterns('',
    (r'^exam/new/$', exam.new),
    (r'^exam/(?P<exam_id>\d+)/edit/$', exam.edit),
    (r'^exam/(?P<exam_id>\d+)/delete/$', exam.delete),
    (r'^exam/search/$', exam.search),
    (r'^exam/(?P<exam_id>\d+)/get_info/$', exam.get_info),
    (r'^exam/(?P<exam_id>\d+)/list_sections/$', exam.list_sections),
    (r'^exam/(?P<exam_id>\d+)/add_section/(?P<section_id>\d+)/$', exam.add_section),
    (r'^exam/(?P<exam_id>\d+)/remove_section/(?P<section_id>\d+)/$', exam.remove_section),
    (r'^exam/(?P<exam_id>\d+)/set_clinician/(?P<user_id>\d+)/$', exam.set_clinician),
)


# Visit API related urls
urlpatterns += patterns('',
    (r'^visit/new/$', visit.new),
    (r'^visit/(?P<visit_id>\d+)/edit/$', visit.edit),
    (r'^visit/(?P<visit_id>\d+)/delete/$', visit.delete),
    (r'^visit/search/$', visit.search),
    (r'^visit/(?P<visit_id>\d+)/get_info/$', visit.get_info),
    (r'^visit/(?P<visit_id>\d+)/get_full_info/$', visit.get_full_info),
    (r'^visit/(?P<visit_id>\d+)/list_exams/$', visit.list_exams),
    (r'^visit/(?P<visit_id>\d+)/add_exam/(?P<exam_id>\d+)/$', visit.add_exam),
    (r'^visit/(?P<visit_id>\d+)/remove_exam/(?P<exam_id>\d+)/$', visit.remove_exam),
    (r'^visit/(?P<visit_id>\d+)/set_parent_visit/(?P<parent_visit_id>\d+)/$', visit.set_parent_visit),
)
