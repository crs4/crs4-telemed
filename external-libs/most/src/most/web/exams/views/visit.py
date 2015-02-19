from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST, require_GET
from django.utils.translation import ugettext as _
import json
from datetime import date, datetime
from django.db.models import Q
from django.contrib.auth.decorators import login_required, user_passes_test
from most.web.exams.views import SUCCESS_KEY, MESSAGE_KEY, TOTAL_KEY, ERRORS_KEY, DATA_KEY
from most.web.exams.forms import VisitForm
from most.web.exams.models import Visit, Exam


@csrf_exempt
@login_required
@require_POST
def new(request):
    results = {}
    try:
        visit_form = VisitForm(request.POST)
        if visit_form.is_valid():
            visit = visit_form.save()
            results[SUCCESS_KEY] = True
            results[MESSAGE_KEY] = _('Visit %s successfully created.' % visit.pk)
            results[DATA_KEY] = visit.to_dictionary(exclude_parent_visit=True)
        else:
            results[SUCCESS_KEY] = False
            results[ERRORS_KEY] = _('Unable to create visit.')
            for field, error in visit_form.errors.items():
                results[ERRORS_KEY] += '\n%s\n' % error
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def edit(request, visit_id):
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        visit_form = VisitForm(request.POST, instance=visit)
        if visit_form.is_valid():
            visit = visit_form.save()
            results[SUCCESS_KEY] = True
            results[MESSAGE_KEY] = _('Visit %s successfully updated.' % visit_id)
            results[DATA_KEY] = visit.to_dictionary(exclude_parent_visit=True)
        else:
            results[SUCCESS_KEY] = False
            results[ERRORS_KEY] = _('Unable to update visit.')
            for field, error in visit_form.errors.items():
                results[ERRORS_KEY] += '\n%s\n' % error
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def delete(request, visit_id):
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        visit.is_active = False
        visit.deactivation_datetime = datetime.now()
        visit.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Visit %s deactivated' % visit_id)
        results[DATA_KEY] = {'id': visit_id, 'is_active': visit.is_active}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@require_GET
def search(request):
    result = {}
    query_set = (Q())
    try:
        query_string = request.GET['query_string']
        query_list = [query for query in query_string.split(' ') if query]
        for query in query_list:
            query_set = query_set & (
                Q(speciality__icontains=query) |
                Q(parent_visit__speciality__icontains=query) |
                Q(notes__icontains=query) |
                Q(reason__icontains=query)
            )
        visits = Visit.objects.filter(query_set)
        count_visits = visits.count()
        result[DATA_KEY] = []
        if count_visits:
            for visit in visits:
                result[DATA_KEY].append(visit.to_dictionary(exclude_parent_visit=True))
            result[SUCCESS_KEY] = True
            result[MESSAGE_KEY] = _('%(visits_count)s visits found for query string: \'%(query_string)s\'' %
                                    {'visits_count': count_visits, 'query_string': query_string})
        else:
            result[SUCCESS_KEY] = False
            result[MESSAGE_KEY] = _('No visits found for query string: \'%s\'' % query_string)
        result[TOTAL_KEY] = count_visits
    except Exception, e:
        result[ERRORS_KEY] = e
        result[SUCCESS_KEY] = False
    return HttpResponse(json.dumps(result), content_type='application/json; charset=utf8')


@login_required
def get_info(request, visit_id):
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Visit %s found.' % visit_id)
        results[DATA_KEY] = visit.to_dictionary(exclude_parent_visit=True)
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
def get_full_info(request, visit_id):
    # also print parent_visit info (depth=1)
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Visit %s found.' % visit_id)
        results[DATA_KEY] = visit.to_dictionary()
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
def list_exams(request, visit_id):
    results = {}
    try:
        exams = Visit.objects.get(pk=visit_id).exams.all()
        exams_list = []
        for exam in exams:
            exams_list.append(exam.to_dictionary())
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Exams of visit %(visit_id)s found.' % {'visit_id': visit_id})
        results[DATA_KEY] = exams_list
        results[TOTAL_KEY] = exams.count()
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def add_exam(request, visit_id, exam_id):
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        exam = Exam.objects.get(pk=exam_id)
        if not visit.exams.filter(pk=exam_id):
            visit.exams.add(exam)
            visit.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Exam %(exam_id)s added to visit %(visit_id)s' %
                                 {'exam_id': exam_id, 'visit_id': visit_id})
        results[DATA_KEY] = {'visit_id': visit_id, 'exam_id': exam_id}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
def remove_exam(request, visit_id, exam_id):
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        exam = Exam.objects.get(pk=exam_id)
        if visit.exams.filter(pk=exam_id):
            visit.exams.remove(exam)
            visit.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Exam %(exam_id)s no more in exams of %(visit_id)s visit' %
                                 {'exam_id': exam_id, 'visit_id': visit_id})
        results[DATA_KEY] = {'exam_id': exam_id, 'visit_id': visit_id}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
def set_parent_visit(request, visit_id, parent_visit_id):
    results = {}
    try:
        visit = Visit.objects.get(pk=visit_id)
        parent_visit = Exam.objects.get(pk=parent_visit_id)
        if not visit.parent_visit.filter(pk=parent_visit_id):
            visit.parent_visit = parent_visit
            visit.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Visit %(parent_visit_id)s is now parent visit of %(visit_id)s visit' %
                                 {'parent_visit_id': parent_visit_id, 'visit_id': visit_id})
        results[DATA_KEY] = {'visit_id': visit_id, 'parent_visit_id': parent_visit_id}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')
