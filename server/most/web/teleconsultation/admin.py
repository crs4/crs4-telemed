# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.contrib import admin
from most.web.teleconsultation.models import Teleconsultation, TeleconsultationSession, Device, Room, ARConfiguration, \
    ARMarker, ARMarkerTranslation, Mesh

admin.site.register(Device)
admin.site.register(Teleconsultation)
admin.site.register(TeleconsultationSession)
admin.site.register(Room)
admin.site.register(ARConfiguration)
admin.site.register(ARMarker)
admin.site.register(ARMarkerTranslation)
admin.site.register(Mesh)
