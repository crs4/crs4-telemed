from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST, require_GET
from django.utils.translation import ugettext as _
import json
from datetime import date, datetime
from django.db.models import Q
from django.contrib.auth.decorators import login_required, user_passes_test
from most.web.exams.views import SUCCESS_KEY, MESSAGE_KEY, TOTAL_KEY, ERRORS_KEY, DATA_KEY
from most.web.exams.forms import ExamForm
from most.web.exams.models import Exam, Section
from most.web.users.models import ClinicianUser


@csrf_exempt
@require_POST
@login_required
def new(request):
    results = {}
    try:
        exam_form = ExamForm(request.POST)
        if exam_form.is_valid():
            exam = exam_form.save()
            results[SUCCESS_KEY] = True
            results[MESSAGE_KEY] = _('Exam %s successfully created.' % exam.pk)
            results[DATA_KEY] = exam.to_dictionary()
        else:
            results[SUCCESS_KEY] = False
            results[ERRORS_KEY] = _('Unable to create section.')
            for field, error in exam_form.errors.items():
                results[ERRORS_KEY] += '\n%s\n' % error
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def edit(request, exam_id):
    results = {}
    try:
        exam = Exam.objects.get(pk=exam_id)
        exam_form = ExamForm(request.POST, instance=exam)
        if exam_form.is_valid():
            exam = exam_form.save()
            results[SUCCESS_KEY] = True
            results[MESSAGE_KEY] = _('Exam %s successfully updated.' % exam_id)
            results[DATA_KEY] = exam.to_dictionary()
        else:
            results[SUCCESS_KEY] = False
            results[ERRORS_KEY] = _('Unable to update exam.')
            for field, error in exam_form.errors.items():
                results[ERRORS_KEY] += '\n%s\n' % error
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def delete(request, exam_id):
    results = {}
    try:
        exam = Exam.objects.get(pk=exam_id)
        exam.is_active = False
        exam.deactivation_datetime = datetime.now()
        exam.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Exam %s deactivated' % exam_id)
        results[DATA_KEY] = {'id': exam_id, 'is_active': exam.is_active}
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
                Q(summary__icontains=query) |
                Q(clinician__user__username__icontains=query) |
                Q(clinician__user__last_name__icontains=query) |
                Q(clinician__user__first_name__icontains=query) |
                Q(clinician__user__email__icontains=query) |
                Q(clinician__user__certified_email__icontains=query) |
                Q(clinician__specialization__icontains=query)
            )
        exams = Exam.objects.filter(query_set)
        count_exams = exams.count()
        result[DATA_KEY] = []
        if count_exams:
            for exam in exams:
                result[DATA_KEY].append(exam.to_dictionary())
            result[SUCCESS_KEY] = True
            result[MESSAGE_KEY] = _('%(exams_count)s exams found for query string: \'%(query_string)s\'' %
                                    {'exams_count': count_exams, 'query_string': query_string})
        else:
            result[SUCCESS_KEY] = False
            result[MESSAGE_KEY] = _('No sections found for query string: \'%s\'' % query_string)
        result[TOTAL_KEY] = count_exams
    except Exception, e:
        result[ERRORS_KEY] = e
        result[SUCCESS_KEY] = False
    return HttpResponse(json.dumps(result), content_type='application/json; charset=utf8')


@login_required
def get_info(request, exam_id):
    results = {}
    try:
        exam = Exam.objects.get(pk=exam_id)
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Exam %s found.' % exam_id)
        results[DATA_KEY] = exam.to_dictionary()
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
def list_sections(request, exam_id):
    results = {}
    try:
        sections = Exam.objects.get(pk=exam_id).sections.all()
        sections_list = []
        for section in sections:
            sections_list.append(section.to_dictionary())
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Sections of exam %(exam_id)s found.' % {'exam_id': exam_id})
        results[DATA_KEY] = sections_list
        results[TOTAL_KEY] = sections.count()
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def add_section(request, exam_id, section_id):
    results = {}
    try:
        exam = Exam.objects.get(pk=exam_id)
        section = Section.objects.get(pk=section_id)
        if not exam.sections.filter(pk=section_id):
            exam.sections.add(section)
            exam.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Section %(section_id)s added to exam %(exam_id)s' %
                                 {'section_id': section_id, 'exam_id': exam_id})
        results[DATA_KEY] = {'exam_id': exam_id, 'section_id': section_id}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
def remove_section(request, exam_id, section_id):
    results = {}
    try:
        exam = Exam.objects.get(pk=exam_id)
        section = Section.objects.get(pk=section_id)
        if exam.sections.filter(pk=section_id):
            exam.sections.remove(section)
            exam.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Section %(section_id)s no more in sections of %(exam_id)s exam' %
                                 {'section_id': section_id, 'exam_id': exam_id})
        results[DATA_KEY] = {'exam_id': exam_id, 'section_id': section_id}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
def set_clinician(request, exam_id, user_id):
    results = {}
    try:
        clinician_user = ClinicianUser.objects.get(user__pk=user_id)
        exam = Exam.objects.get(pk=exam_id)
        exam.clinician = clinician_user
        exam.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Exam %(exam_id)s now has clinician %(user_id)s' %
                                 {'exam_id': exam_id, 'user_id': user_id})
        results[DATA_KEY] = {'exam_id': exam_id, 'user_id': user_id}
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')