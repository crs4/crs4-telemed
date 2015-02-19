# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.shortcuts import render, get_object_or_404, get_list_or_404, render_to_response
from django.http import Http404
from django.core.context_processors import csrf
from django.template import RequestContext
from django.utils.translation import ugettext as _
from django.utils.translation import get_language
from most.web.users.forms import TaskGroupForm, MostUserForm, ClinicianUserForm


def examples(request):
    context = RequestContext(request)
    context.update(csrf(request))
    response = render_to_response('users/demo.html', context)
    response.set_cookie("django_language", get_language())
    return response


def task_group_new(request):
    form = TaskGroupForm()
    return render_to_response('users/task_group/new.html', {'api_description': request.POST['caller'], 'form': form})


def task_group_edit(request):
    form = TaskGroupForm()
    return render_to_response('users/task_group/edit.html', {'api_description': request.POST['caller'], 'form': form})


def task_group_is_provider(request):
    return render_to_response('users/task_group/is_provider.html', {'api_description': request.POST['caller']})


def task_group_set_provider(request):
    return render_to_response('users/task_group/set_provider.html', {'api_description': request.POST['caller']})


def task_group_set_active_state(request):
    return render_to_response('users/task_group/set_active_state.html', {'api_description': request.POST['caller']})


def task_group_add_user(request):
    return render_to_response('users/task_group/add_user.html', {'api_description': request.POST['caller']})


def task_group_remove_user(request):
    return render_to_response('users/task_group/remove_user.html', {'api_description': request.POST['caller']})


def task_group_list_users(request):
    return render_to_response('users/task_group/list_users.html', {'api_description': request.POST['caller']})


def task_group_add_related_task_group(request):
    return render_to_response('users/task_group/add_related_task_group.html', {'api_description': request.POST['caller']})


def task_group_remove_related_task_group(request):
    return render_to_response('users/task_group/remove_related_task_group.html', {'api_description': request.POST['caller']})


def task_group_list_related_task_group(request):
    return render_to_response('users/task_group/list_related_task_group.html', {'api_description': request.POST['caller']})


def task_group_has_clinicians(request):
    return render_to_response('users/task_group/has_clinicians.html', {'api_description': request.POST['caller']})


def task_group_list_clinicians(request):
    return render_to_response('users/task_group/list_clinicians.html', {'api_description': request.POST['caller']})


def task_group_has_clinician_provider(request):
    return render_to_response('users/task_group/has_clinician_provider.html', {'api_description': request.POST['caller']})


def task_group_list_clinician_providers(request):
    return render_to_response('users/task_group/list_clinician_providers.html', {'api_description': request.POST['caller']})


def task_group_search(request):
    return render_to_response('users/task_group/search.html', {'api_description': request.POST['caller']})


def most_user_login(request):
    form = MostUserForm()
    return render_to_response('users/most_user/login.html', {'api_description': request.POST['caller'], 'form': form})


def most_user_logout(request):
    form = MostUserForm()
    return render_to_response('users/most_user/logout.html', {'api_description': request.POST['caller'], 'form': form})


def most_user_new(request):
    form = MostUserForm()
    return render_to_response('users/most_user/new.html', {'api_description': request.POST['caller'], 'form': form})


def most_user_edit(request):
    form = MostUserForm()
    return render_to_response('users/most_user/edit.html', {'api_description': request.POST['caller'], 'form': form})


def most_user_get_user_info(request):
    return render_to_response('users/most_user/get_user_info.html', {'api_description': request.POST['caller']})


def most_user_search(request):
    return render_to_response('users/most_user/search.html', {'api_description': request.POST['caller']})


def most_user_deactivate(request):
    return render_to_response('users/most_user/deactivate.html', {'api_description': request.POST['caller']})


def most_user_activate(request):
    return render_to_response('users/most_user/activate.html', {'api_description': request.POST['caller']})


def clinician_user_new(request):
    form = ClinicianUserForm()
    return render_to_response('users/clinician_user/new.html', {
        'api_description': request.POST['caller'], 'form': form})


def clinician_user_edit(request):
    form = ClinicianUserForm()
    return render_to_response('users/clinician_user/edit.html', {
        'api_description': request.POST['caller'], 'form': form})


def clinician_user_search(request):
    return render_to_response('users/clinician_user/search.html', {'api_description': request.POST['caller']})


def clinician_user_is_provider(request):
    return render_to_response('users/clinician_user/is_provider.html', {'api_description': request.POST['caller']})


def clinician_user_set_provider(request):
    return render_to_response('users/clinician_user/set_provider.html', {'api_description': request.POST['caller']})


def clinician_user_get_user_info(request):
    return render_to_response('users/clinician_user/get_user_info.html', {'api_description': request.POST['caller']})