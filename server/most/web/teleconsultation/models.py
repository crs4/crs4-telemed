# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014-2015, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.db import models
from django.utils.translation import ugettext_lazy as _

from most.web.demographics.models import Patient
from most.web.users.models import MostUser, TaskGroup
from most.web.streaming.models import StreamingDevice
from most.web.utils import pkgen
import time
import calendar


class Device(models.Model):

    """Class Device:

    Attributes:
        uuid                   (django.db.models.CharField)    : unique identifier
    """

    APPLICATION_TYPES = (
        ('AA', 'Applicant App'),
        ('SA', 'Specialist App'),
    )

    DEVICE_PLATFORM = (
        ('IOS', 'Apple platform'),
        ('ANDROID', 'Android platform')
    )

    uuid = models.CharField(_('Device UUID'), max_length=100, unique=True)
    description = models.CharField(_('Description'), max_length=50)
    application_type = models.CharField(_('Application type'), choices=APPLICATION_TYPES, max_length=2)
    device_platform = models.CharField(_('Device platform'), choices=DEVICE_PLATFORM, max_length=7)
    task_groups = models.ManyToManyField(TaskGroup, related_name='devices', null=True, blank=True, verbose_name=_('Device Task Groups'))


    def __unicode__(self):
        if not self.description:
            return '%s in %s' % (self.get_application_type_display(), self.uuid)
        else:
            return '%s in %s' % (self.get_application_type_display(), self.description)


class Room(models.Model):

    uuid = models.CharField(max_length=40, unique=True, default=pkgen)
    name = models.CharField(max_length=200)
    description = models.CharField(max_length=200)
    encoder_device = models.ForeignKey(StreamingDevice, related_name="encoder_rooms", blank=True, null=True)
    camera_device = models.ForeignKey(StreamingDevice, related_name="camera_rooms", blank=True, null=True)
    task_group = models.ForeignKey(TaskGroup, related_name="rooms")
    has_encoder = models.BooleanField(default=True)
    has_camera = models.BooleanField(default=True)
    ar_conf = models.ForeignKey('ARConfiguration', null=True, blank=True, verbose_name='AR configuration')

    def __unicode__(self):
        return '[Room: {name} - {description} - Taskgroup: {tgname}]'.format(name=self.name, description=self.description, tgname=self.task_group.name)

    def _get_json_dict(self):

        return {
            'uuid': self.uuid,
            'name': self.name,
            'description': self.description,
        }

    json_dict = property(_get_json_dict)

    def _get_full_json_dict(self):

        devices = {}
        if self.has_camera:
            devices['camera'] = self.camera_device.json_dict
        if self.has_encoder:
            devices['encoder'] = self.encoder_device.json_dict

        res = {
            'uuid': self.uuid,
            'name': self.name,
            'description': self.description,
            'task_group': self.task_group.json_dict,
            'devices': devices
        }

        if self.ar_conf:
            res['ar_conf'] = self.ar_conf.to_dict()

        return res

    full_json_dict = property(_get_full_json_dict)


class Teleconsultation(models.Model):

    TELECONSULTATION_STATE = (
        ('NEW', 'New Teleconsultation'), #Created from applicant
        ('WAITING', 'At Least one Session Open - (SESSION WAITING STATE)'),
        ('ACTIVE', 'Last Session in progress'),
        ('SESSION_CLOSE', 'Last Session is closed'),
        ('CLOSE', 'Teleconsultation closed')
    )

    URGENCY_STATE = (
        ('LOW', 'Low severity'),
        ('NORMAL', 'Normal severity'),
        ('URGENCY', 'Urgency'),
        ('EMERGENCY', 'Emergency')
    )

    uuid = models.CharField(max_length=40, unique=True, default=pkgen)
    applicant = models.ForeignKey(MostUser, related_name="has_applicant", blank=True, null=True)
    specialist = models.ForeignKey(MostUser, related_name="has_specialist", blank=True, null=True)
    description = models.CharField(_('Description'), max_length=200)
    task_group = models.ForeignKey(TaskGroup, related_name="teleconsultations")
    state = models.CharField(_('Teleconsultation State'), choices=TELECONSULTATION_STATE, max_length=20)
    severity = models.CharField(_('Severity State'), choices=URGENCY_STATE, max_length=20, default="NORMAL")
    created = models.DateTimeField(auto_now_add=True)
    updated = models.DateTimeField(auto_now=True)
    patient = models.ForeignKey(Patient, blank=True, null=True)

    def __unicode__(self):
        return '[Teleconsultation: {uuid} - {description} - Taskgroup: {tgname}]'.format(uuid=self.uuid, description=self.description, tgname=self.task_group.name)

    def _get_json_dict(self):
        
        result  = {
            'uuid': self.uuid,
            'description': self.description,
            'created': calendar.timegm(self.created.timetuple()),
            'severity': self.severity,
            'patient': {'id': self.patient.uid, 'account_number': self.patient.account_number,
                        'first_name': self.patient.first_name, 'last_name': self.patient.last_name,
                        'gender': self.patient.gender, 'birth_date': calendar.timegm(self.patient.birth_date.timetuple())}
            if self.patient is not None else None,
            'applicant' : { 'firstname': self.applicant.first_name, 'lastname': self.applicant.last_name,
                'username' : self.applicant.username , 'voip_data' : None if len(self.applicant.account_set.all())<1  else  self.applicant.account_set.all()[0].json_dict},
            'specialist' : None if self.specialist == None else { 'username' : self.specialist.get_full_name() ,
                                         'voip_data' : None if len(self.specialist.account_set.all())<1  else self.specialist.account_set.all()[0].json_dict}
        }

        #Check sessions
        if self.sessions.count() > 0:
            last_session = self.sessions.order_by('-created')[0]
            result['last_session'] = last_session.full_json_dict

        return result

    json_dict = property(_get_json_dict)

    def _get_full_json_dict(self):

        return {
            'uuid': self.uuid,
            'description': self.description,
            'task_group': self.task_group.json_dict,
            'applicant' : { 'firstname': self.applicant.first_name, 'lastname': self.applicant.last_name,
                            'username' : self.applicant.username,
                            'voip_data' : None if len(self.applicant.account_set.all())<1  else  self.applicant.account_set.all()[0].json_dict},
            'specialist' : None if self.specialist == None else { 'username' : self.specialist.get_full_name() ,
                                         'voip_data' : None if len(self.specialist.account_set.all())<1  else self.specialist.account_set.all()[0].json_dict}
        }

    full_json_dict = property(_get_full_json_dict)


class TeleconsultationSession(models.Model):

    SESSION_STATE = (
        ('NEW', 'New Session'), #Created from applicant
        ('WAITING', 'Session waiting for specialist'), #Started from applicant
        ('READY', 'Session ready to start'), #Started from applicant
        ('RUN', 'Session in progress'), #Accepted by Specialist
        ('CLOSE', 'Session is closed'), #Closed from applicant or specialist
        ('CANCELED', 'Session is canceled') #Canceled from applicant or specialist
    )

    uuid = models.CharField(max_length=40, unique=True, default=pkgen)
    teleconsultation = models.ForeignKey(Teleconsultation, related_name="sessions")
    state = models.CharField(_('Teleconsultation Session State'), choices=SESSION_STATE, max_length=20, default='NEW')
    created = models.DateTimeField(auto_now_add=True)
    updated = models.DateTimeField(auto_now=True)
    room = models.ForeignKey(Room, related_name="sessions")
    spec_app_address = models.CharField(max_length=15, default="")

    def __unicode__(self):
        return '[Teleconsultation Session: {uuid}]'.format(uuid=self.uuid)

    def _get_json_dict(self):

        return {
            'uuid': self.uuid,
            'created': calendar.timegm(self.created.timetuple()),
            'updated': calendar.timegm(self.created.timetuple()),
            'state': self.state,
            'spec_app_address': self.spec_app_address
        }

    json_dict = property(_get_json_dict)

    def _get_full_json_dict(self):

        result = self.json_dict
        result.update({
            'teleconsultation': self.teleconsultation.full_json_dict,
            'room': self.room.full_json_dict
        })
        return result

    full_json_dict = property(_get_full_json_dict)


class ARConfiguration(models.Model):
    eco_marker = models.ForeignKey('ARMarker', null=True,blank=True, related_name='eco_configurations')
    keyboard_marker = models.ForeignKey('ARMarker', null=True,blank=True, related_name='keyboard_configurations')
    patient_marker = models.ForeignKey('ARMarker', null=True,blank=True, related_name='patient_configurations')
    screen_height = models.FloatField(null=True, blank=True, help_text="expressed in mm")
    screen_width = models.FloatField(null=True, blank=True, help_text="expressed in mm")
    description = models.CharField(max_length=200, null=True, blank=True)

    class Meta:
        verbose_name = 'AR Configuration'
        verbose_name_plural = 'AR Configurations'

    def __str__(self):
        return self.description

    def to_dict(self):
        return {
            'eco_marker': self.eco_marker.to_dict() if self.eco_marker else None,
            'keyboard_marker': self.key_marker.to_dict() if self.keyboard_marker else None,
            'patient_marker': self.patient_marker.to_dict() if self.patient_marker else None,
            'screen_height': self.screen_height,
            'screen_width': self.screen_width
        }


class ARMarker(models.Model):
    path = models.CharField(max_length=200)
    type = models.CharField(max_length=10, choices=(("multi", "multi"), ("single", "single")), default="single")
    size = models.IntegerField(default=80, help_text="expressed in mm")

    trans_x = models.FloatField(default=0.0, help_text="expressed in mm")
    trans_y = models.FloatField(default=0.0, help_text="expressed in mm")

    class Meta:
        verbose_name = 'AR Marker'
        verbose_name_plural = 'AR Markers'

    def __str__(self):
        return "%s;%s;%s" % (self.type, self.path, self.size)

    def to_dict(self):
        return {'conf': str(self), 'trans_x': self.trans_x, 'trans_y': self.trans_y}