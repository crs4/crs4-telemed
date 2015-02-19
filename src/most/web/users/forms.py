# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.forms import ModelForm
from django.core.exceptions import ValidationError
from django.utils.translation import ugettext_lazy as _
from most.web.users.models import MostUser, TaskGroup, ClinicianUser


class TaskGroupForm(ModelForm):
    def __init__(self, *args, **kwargs):
        super(TaskGroupForm, self).__init__(*args, **kwargs)
        self.fields['users'].queryset = MostUser.objects.filter(task_group_related__isnull=True)
        self.fields['related_task_groups'].queryset = TaskGroup.objects.filter(task_group_type='HF')

    def clean(self):
        cleaned_data = super(TaskGroupForm, self).clean()
        related_task_groups = cleaned_data.get("related_task_groups")
        is_health_care_provider = cleaned_data.get("is_health_care_provider")
        # If is_health_care_provider == False and related_task_groups not null, raise exception
        if related_task_groups and not is_health_care_provider:
            raise ValidationError(_('Only health care facilities have related task group.'))
        return cleaned_data

    class Meta:
        model = TaskGroup
        fields = [
            'title',
            'description',
            'task_group_type',
            'hospital',
            'users',
            'is_health_care_provider',
            'is_active',
            'related_task_groups',
        ]


class MostUserForm(ModelForm):
    class Meta:
        model = MostUser
        fields = [
            'username',
            'first_name',
            'last_name',
            'email',
            'birth_date',
            'is_active',
            'is_admin',
            'numeric_password',
            'user_type',
            'gender',
            'phone',
            'mobile',
            'certified_email',
        ]


class ClinicianUserForm(ModelForm):
    def __init__(self, *args, **kwargs):
        super(ClinicianUserForm, self).__init__(*args, **kwargs)
        self.fields['user'].queryset = MostUser.objects.filter(clinician_related__isnull=True, user_type='CL')

    class Meta:
        model = ClinicianUser
        fields = [
            'user',
            'clinician_type',
            'specialization',
            'is_health_care_provider',
        ]
