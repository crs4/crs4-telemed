from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST, require_GET
from django.utils.translation import ugettext as _
import json
from datetime import date, datetime
from django.db.models import Q
from django.contrib.auth.decorators import login_required, user_passes_test
from most.web.exams.views import SUCCESS_KEY, MESSAGE_KEY, TOTAL_KEY, ERRORS_KEY, DATA_KEY
from most.web.exams.forms import SectionForm
from most.web.exams.models import Section


@csrf_exempt
@login_required
@require_POST
def new(request):
    results = {}
    try:
        section_form = SectionForm(request.POST)
        if section_form.is_valid():
            section = section_form.save()
            results[SUCCESS_KEY] = True
            results[MESSAGE_KEY] = _('Section %s successfully created.' % section.pk)
            results[DATA_KEY] = section.to_dictionary()
        else:
            results[SUCCESS_KEY] = False
            results[ERRORS_KEY] = _('Unable to create section.')
            for field, error in section_form.errors.items():
                results[ERRORS_KEY] += '\n%s\n' % error
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def edit(request, section_id):
    results = {}
    try:
        section = Section.objects.get(pk=section_id)
        section_form = SectionForm(request.POST, instance=section)
        if section_form.is_valid():
            section = section_form.save()
            results[SUCCESS_KEY] = True
            results[MESSAGE_KEY] = _('Section %s successfully updated.' % section_id)
            results[DATA_KEY] = section.to_dictionary()
        else:
            results[SUCCESS_KEY] = False
            results[ERRORS_KEY] = _('Unable to update section.')
            for field, error in section_form.errors.items():
                results[ERRORS_KEY] += '\n%s\n' % error
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')


@login_required
@csrf_exempt
@require_POST
def delete(request, section_id):
    results = {}
    try:
        section = Section.objects.get(pk=section_id)
        section.is_active = False
        section.deactivation_datetime = datetime.now()
        section.save()
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Section %s deactivated' % section_id)
        results[DATA_KEY] = {'id': section_id, 'is_active': section.is_active}
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
                Q(name__icontains=query)
            )
        sections = Section.objects.filter(query_set)
        count_sections = sections.count()
        result[DATA_KEY] = []
        if count_sections:
            for section in sections:
                result[DATA_KEY].append(section.to_dictionary())
            result[SUCCESS_KEY] = True
            result[MESSAGE_KEY] = _('%(sections_count)s sections found for query string: \'%(query_string)s\'' %
                                    {'sections_count': count_sections, 'query_string': query_string})
        else:
            result[SUCCESS_KEY] = False
            result[MESSAGE_KEY] = _('No sections found for query string: \'%s\'' % query_string)
        result[TOTAL_KEY] = count_sections
    except Exception, e:
        result[ERRORS_KEY] = e
        result[SUCCESS_KEY] = False
    return HttpResponse(json.dumps(result), content_type='application/json; charset=utf8')


@login_required
def get_info(request, section_id):
    results = {}
    try:
        section = Section.objects.get(pk=section_id)
        results[SUCCESS_KEY] = True
        results[MESSAGE_KEY] = _('Section %s found.' % section_id)
        results[DATA_KEY] = section.to_dictionary()
    except Exception, e:
        results[SUCCESS_KEY] = False
        results[ERRORS_KEY] = e
    return HttpResponse(json.dumps(results), content_type='application/json; charset=utf8')
