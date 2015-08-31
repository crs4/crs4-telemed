#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt
#


from django.shortcuts import render
from django.views.decorators.csrf import csrf_exempt
import datetime, json
from django.core.exceptions import ObjectDoesNotExist
from django.http import HttpResponse
from most.web.authentication.decorators import oauth2_required
from most.web.voip.models import Account, Buddy
from most.web.teleconsultation.models import Device, Teleconsultation, TeleconsultationSession, Room
from most.web.users.models import TaskGroup
from django.utils.translation import ugettext_lazy as _
import logging
from datetime import datetime, timedelta, time

# Get an instance of a logger
logger = logging.getLogger('most.web.teleconsultation')

@csrf_exempt
def get_task_groups_for_device(request, device_uuid):
    """
    Return the list of active taskgroups for selected device
    :param device_uuid: string that represent device universally unique id
    :return: a list of active taskgroups for selected device:
    """
    # check if device uuid exists
    logger.info('Try to retrieve device with uuid %s' % device_uuid)

    try:
        device = Device.objects.get(uuid=device_uuid)
        logger.info('Retrieved device: %s' % device)
        task_groups = []
        for task_group in device.task_groups.all():
            logger.info('Append Taskgroup: %s' % task_group)
            task_groups.append({'uuid': task_group.uuid, 'name': task_group.name, 'description': task_group.description})
        return HttpResponse(json.dumps({'success': True, 'data': {'task_groups': task_groups}}), content_type="application/json")

    except Device.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid device uuid', "device_uuid": device_uuid}}), content_type="application/json")

@csrf_exempt
def get_applicants_for_taskgroups(request, taskgroup_uuid):
    """
    Return the list of valid applicant users based on device id (and implicitly taskgroup)
    :param request:
    :return: a list of valid applicants
    """

    logger.info("Try to retrieve applicants for taskgroup %s" % taskgroup_uuid)

    try:
        taskgroup = TaskGroup.objects.get(uuid=taskgroup_uuid)
        applicants = []
        for applicant in taskgroup.users.exclude(user_type='ST').exclude(user_type="TE"):
            applicants.append({"firstname": applicant.first_name, "lastname" : applicant.last_name, "username": applicant.username, "voip_extension" : applicant.voip_extension})
        return HttpResponse(json.dumps({'success': True, 'data': {'applicants': applicants}}), content_type="application/json")

    except TaskGroup.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid taskgroup uuid'}}), content_type="application/json")

""" AUTHENTICATED APIs """
@oauth2_required
def get_rooms_for_taskgroup(request):
    """
    Return the list of teleconsultation rooms for selected taskgroup
    :param taskgroup_uuid: string that represent taskgroup universally unique id
    :return: a list of rooms
    """
    rooms = []

    try:
        taskgroup = request.taskgroup

        for room in taskgroup.rooms.all():
            rooms.append(room.json_dict)

        return HttpResponse(json.dumps({'success': True, 'data': {'rooms': rooms}}), content_type="application/json")

    except TaskGroup.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid taskgroup uuid'}}), content_type="application/json")


@oauth2_required
def get_room_by_uuid(request, room_uuid):
    """
    Return the full details of selected room
    :param request: http request
    :param room_uuid: uuid of request room
    :return: a json with room details
    """
    try:
        room = Room.objects.get(uuid=room_uuid)
        return HttpResponse(json.dumps({'success': True, 'data': {'room': room.full_json_dict}}), content_type="application/json")

    except Room.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid room uuid'}}), content_type="application/json")

@oauth2_required
@csrf_exempt
def create_teleconsultation(request):
    """
    POST: Add new teleconsultation with current user as applicant
    :param description: free text for describe teleconsutation
    :param severity: the severity level of teleconsultation (LOW, NORMAL, URGENCY, EMERGENCY)- default NORMAL
    :param: room_uuid: the uuid of selected room for new teleconsultation
    :return: the uuid of created teleconsultation
    """
    # check parameters
    if set(['room_uuid', 'description']) > set(request.REQUEST):
       return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'missing parameters'}}), content_type="application/json")

    # check and retrieve room
    room = None
    try:
        room = Room.objects.get(uuid=request.REQUEST['room_uuid'])

    except Room.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 502, 'message': 'invalid room uuid'}}), content_type="application/json")

    # make teleconsultation
    teleconsultation = Teleconsultation()
    teleconsultation.applicant = request.user
    teleconsultation.description = request.REQUEST['description']
    teleconsultation.state = 'NEW'
    teleconsultation.task_group = request.taskgroup
    if 'severity' in request.REQUEST:
        teleconsultation.severity = request.REQUEST['severity']

    teleconsultation.save()

    return HttpResponse(json.dumps({'success': True, 'data': {'teleconsultation' : teleconsultation.json_dict}}), content_type="application/json")


@oauth2_required
@csrf_exempt
def close_teleconsultation(request):
    return HttpResponse(json.dumps({'success': False, 'error': {'message' : 'not implemented'}}), content_type="application/json")


@oauth2_required
@csrf_exempt
def get_teleconsultations(request):
    """
    :param request:
    :return: a list of opened teleconsultation for device task-groups
    """

    today = datetime.now().date()
    tomorrow = today + timedelta(1)
    today_start = datetime.combine(today, time())
    today_end = datetime.combine(tomorrow, time())
    today_start = datetime.combine(today, time())

    logging.error("Taskgroup from token: %s" % request.accesstoken.taskgroup)

    teleconsultations = Teleconsultation.objects.filter(task_group=request.taskgroup, updated__lte=today_end, updated__gte=today_start)

    teleconsultation_list = []

    for teleconsultation in teleconsultations:

        teleconsultation_list.append(teleconsultation.json_dict)


    result = {

        "teleconsultations": teleconsultation_list
    }
    return HttpResponse(json.dumps({'success': True, 'data': result}), content_type="application/json")


@oauth2_required
@csrf_exempt
def create_new_session(request, teleconsultation_uuid):

    import logging
    logging.error("IN CREATE NEW SESSION: %s" % teleconsultation_uuid)
    # Check and Retrieve teleconsultation
    teleconsultation = None
    try:
        teleconsultation = Teleconsultation.objects.get(uuid=teleconsultation_uuid)

    except Teleconsultation.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid teleconsultation uuid'}}), content_type="application/json")

    # Check and Retrieve room
    room = None
    try:
        room = Room.objects.get(uuid=request.REQUEST['room_uuid'])

    except Room.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 502, 'message': 'invalid room uuid'}}), content_type="application/json")

    # Check last session state
    if teleconsultation.sessions.count() > 0:
        last_session = teleconsultation.sessions.order_by('-created')[0]
        if last_session.state != 'CLOSE':
            return HttpResponse(json.dumps({'success': False, 'error': {'code': 503, 'message': 'invalid last session state'}}), content_type="application/json")

    # Create session
    session = TeleconsultationSession()
    session.teleconsultation = teleconsultation
    session.room = room

    session.save()

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'saved', 'session': session.json_dict}}), content_type="application/json")


@csrf_exempt
@oauth2_required
def start_session(request, session_uuid):

    # Check and Retrieve session
    session = None
    try:
        session = TeleconsultationSession.objects.get(uuid=session_uuid)

    except TeleconsultationSession.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid session uuid'}}), content_type="application/json")

    if session.state != "NEW":
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 502, 'message': 'invalid session state'}}), content_type="application/json")

    session.state = 'WAITING'
    session.teleconsultation.state = 'OPEN'
    session.teleconsultation.save()
    session.save()

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'saved', 'session': session.json_dict}}), content_type="application/json")


@csrf_exempt
@oauth2_required
def join_session(request,session_uuid):
   # Check and Retrieve session
    session = None
    try:
        session = TeleconsultationSession.objects.get(uuid=session_uuid)

    except TeleconsultationSession.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid session uuid'}}), content_type="application/json")

    if session.state != "WAITING":
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 502, 'message': 'invalid session state'}}), content_type="application/json")

    session.state = 'READY'
    session.teleconsultation.save()
    session.save()

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'saved', 'session': session.json_dict}}), content_type="application/json")


@csrf_exempt
@oauth2_required
def run_session(request,session_uuid):
   # Check and Retrieve session
    session = None
    try:
        session = TeleconsultationSession.objects.get(uuid=session_uuid)

    except TeleconsultationSession.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid session uuid'}}), content_type="application/json")

    if session.state != "READY":
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 502, 'message': 'invalid session state'}}), content_type="application/json")

    session.state = 'RUN'
    session.teleconsultation.state = 'ACTIVE'
    session.teleconsultation.save()
    session.save()

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'saved', 'session': session.json_dict}}), content_type="application/json")


@csrf_exempt
@oauth2_required
def close_session(request,session_uuid):
   # Check and Retrieve session
    session = None
    try:
        session = TeleconsultationSession.objects.get(uuid=session_uuid)

    except TeleconsultationSession.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid session uuid'}}), content_type="application/json")

    if session.state == "CLOSE":
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 502, 'message': 'invalid session state'}}), content_type="application/json")

    session.state = 'CLOSE'
    session.save()

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'saved', 'session': session.json_dict}}), content_type="application/json")    
    

@oauth2_required
def get_session_data(request, session_uuid):

    # Check and Retrieve session
    session = None
    try:
        session = TeleconsultationSession.objects.get(session_uuid)

    except TeleconsultationSession.DoesNotExist:
        return HttpResponse(json.dumps({'success': False, 'error': {'code': 501, 'message': 'invalid session uuid'}}), content_type="application/json")

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'retrieved', 'session': session.json_dict}}), content_type="application/json")


@oauth2_required
def get_open_teleconsultations(request):

    """
    :param request:
    :return: a list of today opened teleconsultation for applicant task-groups and related applicant task-group
    """

    today = datetime.now().date()
    tomorrow = today + timedelta(1)
    today_start = datetime.combine(today, time())
    today_end = datetime.combine(tomorrow, time())
    today_start = datetime.combine(today, time())

    taskgroups = set()
    for taskgroup in request.user.task_group_related.all():
        for relatedTaskgroup in taskgroup.related_task_groups.all():
            taskgroups.add(relatedTaskgroup) # taskgroup

        taskgroups.add(taskgroup) # relatedTaskgroup

    logging.error("Taskgroup from token: %s" % request.accesstoken.taskgroup)

    teleconsultations = Teleconsultation.objects.filter(task_group__in=taskgroups, updated__lte=today_end, updated__gte=today_start, state="OPEN")

    teleconsultation_list = []

    for teleconsultation in teleconsultations:

        teleconsultation_list.append(teleconsultation.json_dict)


    result = {

        "teleconsultations": teleconsultation_list
    }
    return HttpResponse(json.dumps({'success': True, 'data': result}), content_type="application/json")


@oauth2_required
def test(request):

    return HttpResponse(json.dumps({'success': True, 'data': {'message': 'Hello Teleconsultation'}}), content_type="application/json")
