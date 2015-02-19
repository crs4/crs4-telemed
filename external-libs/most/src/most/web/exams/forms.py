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
from most.web.exams.models import Section, Exam, Visit


class SectionForm(ModelForm):
    class Meta:
        model = Section


class ExamForm(ModelForm):
    class Meta:
        model = Exam
        exclude = [
            'creation_datetime',
            'modification_datetime',
            'deactivation_datetime',
        ]


class VisitForm(ModelForm):
    class Meta:
        model = Visit
        exclude = [
            'creation_datetime',
            'modification_datetime',
            'deactivation_datetime',
        ]