#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.shortcuts import render
import datetime, json
from provider.oauth2.models import AccessToken
from most.web.authentication.decorators import oauth2_required
from django.http import HttpResponse
import pytz


@oauth2_required
def test_auth(request):

    return HttpResponse(json.dumps({'success' : True, 'data' : {'username' : request.user.username}}), content_type="application/json")


