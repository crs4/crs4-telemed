# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.db import models
from django.utils.translation import ugettext_lazy as _
from most.web.users.models import ClinicianUser


MEDICAL_SERVICE_CHOICES = (
    ('AMB', _('Ambulatory')),
    ('OUT', _('Outpatient')),
    ('IN', _('Inpatient')),
)

URGENCY_CHOICES = (
    ('GREEN', 'Normal'),
    ('YELLOW', 'Urgent'),
    ('RED', 'Medical Emergency'),
)

STATE_CHOICES = (
    ('N', 'New'),
    ('E', 'Editing'),
    ('S', 'Saved'),
    ('C', 'Closed'),
    ('R', 'Reported'),
)


class Section(models.Model):
    name = models.CharField(_('Section Name'), max_length=25)
    is_active = models.BooleanField(_('Is active (set to false for deletion)?'), default=True)

    def __unicode__(self):
        return u'%s' % self.name

    def to_dictionary(self):
        section_dictionary = {
            u'id': u'%s' % self.id,
            u'name': u'%s' % self.name,
            u'is_active': self.is_active,
        }
        return section_dictionary

    class Meta:
        verbose_name = _("Section")
        verbose_name_plural = _("Sections")


class Exam(models.Model):
    medical_service = models.CharField(_('Medical Service'), max_length=3, choices=MEDICAL_SERVICE_CHOICES)
    speciality = models.CharField(_('Health Speciality'), max_length=25)
    urgency = models.CharField(_('Urgency'), choices=URGENCY_CHOICES, max_length=6, null=True, blank=True)
    state = models.CharField(max_length=1, choices=STATE_CHOICES, default='N')
    summary = models.TextField(_('Summary'), max_length=500, null=True, blank=True)
    creation_datetime = models.DateTimeField(_('Creation DateTime'), auto_now_add=True)
    modification_datetime = models.DateTimeField(_('Modification DateTime'), auto_now=True)
    deactivation_datetime = models.DateTimeField(_('Deactivation Date Time'), null=True, blank=True)
    start_datetime = models.DateTimeField(_('Start DateTime'), null=True, blank=True)
    end_datetime = models.DateTimeField(_('End DateTime'), null=True, blank=True)
    clinician = models.ForeignKey(ClinicianUser, verbose_name='Clinician')
    sections = models.ManyToManyField(Section, related_name='exams_section')
    is_active = models.BooleanField(_('Is active (set to false for deletion)?'), default=True)
    is_remote = models.BooleanField(_('Is the doctor remote?'), default=False)

    def __unicode__(self):
        exam_string = u'%s - %s (%s)' % (self.get_state_display(), self.speciality, self.get_exam_type_display())
        if self.start_datetime:
            exam_string = u'%s | START %s' % (exam_string, self.start_datetime)
        if self.end_datetime:
            exam_string = u'%s | END %s' % (exam_string, self.end_datetime)
        if self.urgency:
            exam_string = u'%s -> %s' % (exam_string, self.geturgency_display())
        return exam_string

    def to_dictionary(self):
        exam_dictionary = {
            u'id': u'%s' % self.id,
            u'medical_service': {
                u'key': u'%s' % self.medical_service,
                u'value': u'%s' % self.get_medical_service_display(),
            },
            u'speciality': self.speciality,
            u'urgency': {
                u'key': u'%s' % self.urgency,
                u'value': u'%s' % self.get_urgency_display()
            },
            u'state': {
                u'key': u'%s' % self.state,
                u'value': u'%s' % self.get_state_display()
            },
            u'summary': u'%s' % self.summary if self.summary else None,
            u'start_datetime':  u'%s' % self.start_datetime.strftime('%d %b %Y') if self.start_datetime else None,
            u'end_datetime': u'%s' % self.end_datetime.strftime('%d %b %Y') if self.end_datetime else None,
            u'clinician': u'%s' % self.clinician,
            # u'clinician': self.clinician.to_dictionary(exclude_user=True),
            u'is_active': self.is_active,
            u'is_remote': self.is_remote,
        }
        if self.sections:
            exam_dictionary[u'sections'] = []
            for section in self.sections:
                exam_dictionary[u'sections'].append(section.to_dictionary())
        return exam_dictionary

    class Meta:
        verbose_name = _("Exam")
        verbose_name_plural = _("Exams")


class Visit(models.Model):
    VISIT_TYPE_CHOICES = (
        ('NEW', _('New health condition')),
        ('FOLLOWUP', 'Followup'),
        ('CHRONIC', 'Checkup for chronic condition'),
        ('CHILD', 'Child routine checkup'),
        ('WOMAN', 'Woman routine checkup'),
        ('MAN', 'Man routine checkup'),
    )

    speciality = models.CharField(_('Clinical speciality'), null=True, blank=True, max_length=50)
    parent_visit = models.ForeignKey('self')
    medical_service = models.CharField(_('Medical Service'), max_length=3, choices=MEDICAL_SERVICE_CHOICES)
    visit_type = models.CharField(_('Visit Type'), max_length=8, choices=VISIT_TYPE_CHOICES, default='NEW')
    urgency = models.CharField(_('Urgency'), choices=URGENCY_CHOICES, max_length=6, null=True, blank=True)
    state = models.CharField(max_length=1, choices=STATE_CHOICES, default='N')
    creation_datetime = models.DateTimeField(_('Creation DateTime'), auto_now_add=True)
    modification_datetime = models.DateTimeField(_('Modification DateTime'), auto_now=True)
    deactivation_datetime = models.DateTimeField(_('Deactivation Date Time'), null=True, blank=True)
    start_datetime = models.DateTimeField(_('Start DateTime'), null=True, blank=True)
    end_datetime = models.DateTimeField(_('End DateTime'), null=True, blank=True)
    exams = models.ManyToManyField(Exam)
    notes = models.TextField(_('Notes'), max_length=500, null=True, blank=True)
    reason = models.TextField(_('Reason'), max_length=200, null=True, blank=True)
    requesting_clinician = models.CharField(_('Requesting Clinician'), max_length=50,  null=True, blank=True)
    is_active = models.BooleanField(_('Is active (set to false for deletion)?'), default=True)

    def __unicode__(self):
        visit_string = u'%s - %s (%s)' % (self.get_state_display(), self.speciality, self.get_visit_type_display())
        if self.start_datetime:
            visit_string = u'%s | START %s' % (visit_string, self.start_datetime)
        if self.end_datetime:
            visit_string = u'%s | END %s' % (visit_string, self.end_datetime)
        if self.urgency:
            visit_string = u'%s -> %s' % (visit_string, self.geturgency_display())
        return visit_string

    def to_dictionary(self, exclude_parent_visit=False):
        visit_dictionary = {
            u'id': u'%s' % self.id,
            u'medical_service': {
                u'key': u'%s' % self.medical_service,
                u'value': u'%s' % self.get_medical_service_display(),
            },
            u'speciality': self.speciality,
            u'urgency': {
                u'key': u'%s' % self.urgency,
                u'value': u'%s' % self.get_urgency_display()
            },
            u'visit_type': {
                u'key': u'%s' % self.visit_type,
                u'value': u'%s' % self.get_visit_type_display()
            },
            u'state': {
                u'key': u'%s' % self.state,
                u'value': u'%s' % self.get_state_display()
            },
            u'notes': u'%s' % self.notes if self.notes else None,
            u'reason': u'%s' % self.reason if self.reason else None,
            u'requesting_clinician': u'%s' % self.requesting_clinician if self.requesting_clinician else None,
            u'start_datetime':  u'%s' % self.start_datetime.strftime('%d %b %Y') if self.start_datetime else None,
            u'end_datetime': u'%s' % self.end_datetime.strftime('%d %b %Y') if self.end_datetime else None,
            u'is_active': self.is_active,
        }
        if not exclude_parent_visit:
            visit_dictionary[u'parent_visit'] = self.parent_visit.to_dictionary(exclude_parent_visit=True)
        if self.exams:
            visit_dictionary[u'exams'] = []
            for exam in self.exams:
                visit_dictionary[u'exams'].append(exam.to_dictionary())
        return visit_dictionary

    class Meta:
        verbose_name = _("Visit")
        verbose_name_plural = _("Visits")


# These class are for possible implementation

class Anamnesys(Exam):
    pass


class PhysicalExamination(Exam):
    pass


class Ecg(Exam):
    pass


class Ecography(Exam):
    pass


class PediatricCardiologyVisit(Visit):
    anamnesys = models.ForeignKey(Anamnesys) # throw visit_exam table?
    physical_examination = models.ForeignKey(PhysicalExamination) # throw visit_exam table?
    ecg = models.ForeignKey(Ecg) # throw visit_exam table?
    ecography = models.ForeignKey(Ecography) # throw visit_exam table?
    generic = models.ForeignKey(Exam) # throw visit_exam table?
