# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django.test import TestCase
from django.core.urlresolvers import reverse
from most.web.users.models import MostUser, ClinicianUser, TaskGroup
from datetime import datetime
import json
from django.db.models import Q


class TaskGroupAPITest(TestCase):
    """ TaskGroup API Test """

    def setUp(self):
        self.service_provider_task_group = TaskGroup(
            title='SERVICE PROVIDER TASK GROUP',
            description='Service Provider Task Group created for test purpose',
            task_group_type='SP',
            hospital='TEST Hospital',
            is_health_care_provider=False
        )
        self.service_provider_task_group.save()
        self.health_care_provider_task_group = TaskGroup(
            title='HEALTH CARE PROVIDER TASK GROUP',
            description='Health Care Provider Task Group created for test purpose',
            task_group_type='HF',
            hospital='TEST Hospital',
            is_health_care_provider=True
        )
        self.health_care_provider_task_group.save()
        self.health_care_consumer_task_group = TaskGroup(
            title='HEALTH CARE CONSUMER TASK GROUP',
            description='Health Care Consumer Task Group created for test purpose',
            task_group_type='HF',
            hospital='TEST Hospital',
            is_health_care_provider=False
        )
        self.health_care_consumer_task_group.save()
        self.user = MostUser.objects.create_user(username='test', first_name='test', last_name='test',
                                                 email='test@most.crs4.it', user_type='CL', password='test')
        self.user.is_admin = True
        self.user.save()
        self.client.login(username='test', password='test')

    # TaskGroup API tests
    def test_task_group_search(self):
        """
        Test API: /users/task_group/search/
        """
        query_string = 'provider'
        response = self.client.get('/users/task_group/search/', data={'query_string': query_string})
        task_groups = json.loads(response.content)
        test_task_groups = [
            self.service_provider_task_group.to_dictionary(exclude_users=True, exclude_related_task_groups=True),
            self.health_care_provider_task_group.to_dictionary(exclude_users=True, exclude_related_task_groups=True)
        ]
        self.assertListEqual(test_task_groups, task_groups['data'], 'test_task_group_search --> KO')

    def test_task_group_new(self):
        """
        Test API: /users/task_group/new/
        """
        data = {
            'title': 'TEST',
            'description': 'TEST',
            'task_group_type': 'SP',
            'hospital': 'TEST Hospital',
            'is_health_care_provider': False,
            'is_active': True,
        }
        response = self.client.post('/users/task_group/new/', data=data)
        task_group = json.loads(response.content)
        test_task_group = {
            u'task_group_type': {
                u'key': u'SP',
                u'value': u'Service Provider'
            },
            u'description': u'TEST',
            u'title': u'TEST',
            u'hospital': u'TEST Hospital',
            u'is_active': True,
            u'is_health_care_provider': False
        }
        task_group['data'].pop('id')
        self.assertEqual(test_task_group, task_group['data'], 'test_task_group_new --> KO')

    def test_task_group_get_task_group_info(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/get_task_group_info/
        """
        response = self.client.get('/users/task_group/%s/get_task_group_info/' % self.health_care_provider_task_group.pk)
        task_group = json.loads(response.content)
        test_task_group = self.health_care_provider_task_group.to_dictionary(
            exclude_related_task_groups=True, exclude_users=True)
        self.assertEqual(test_task_group, task_group['data'], 'test_task_group_get_task_group_info --> KO')

    def test_task_group_edit(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/edit/
        """
        data = {
            u'task_group_type': self.health_care_provider_task_group.task_group_type,
            u'description': self.health_care_provider_task_group.description,
            u'title': u'FIRST HEALTH CARE PROVIDER TASK GROUP',
            u'hospital': self.health_care_provider_task_group.hospital,
            u'is_active': self.health_care_provider_task_group.is_active,
            u'is_health_care_provider': self.health_care_provider_task_group.is_health_care_provider
        }
        response = self.client.post('/users/task_group/%s/edit/' % self.health_care_provider_task_group.pk,
                                    data=data)
        task_group = json.loads(response.content)
        test_task_group = self.health_care_provider_task_group.to_dictionary(
            exclude_related_task_groups=True, exclude_users=True
        )
        test_task_group['title'] = data['title']
        self.assertEqual(test_task_group, task_group['data'], 'test_task_group_edit --> KO')

    def test_task_group_list_available_states(self):
        """
        Test API: /users/task_group/list_available_states/
        """
        response = self.client.get('/users/task_group/list_available_states/')
        activation_states = json.loads(response.content)
        self.assertEqual(TaskGroup.ACTIVATION_STATES, activation_states['data'],
                         'test_task_group_list_available_states --> KO')

    def test_task_group_set_active_state(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/set_active_state/(?P<active_state>\w+)/
        """
        next_activation_state = 'inactive' if self.health_care_consumer_task_group.is_active else 'active'
        response = self.client.post('/users/task_group/%s/set_active_state/%s/' %
                                    (self.health_care_consumer_task_group.pk, next_activation_state))
        task_group_activation_state = json.loads(response.content)
        test_task_group_activation_state = {
            'id': '%s' % self.health_care_consumer_task_group.pk,
            'is_active': False if next_activation_state == 'inactive' else True
        }
        self.assertEqual(test_task_group_activation_state, task_group_activation_state['data'],
                         'test_task_group_set_active_state --> KO')

    def test_task_group_is_provider(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/is_provider/
        """
        response = self.client.get('/users/task_group/%s/is_provider/' % self.health_care_consumer_task_group.pk)
        task_group_is_provider = json.loads(response.content)
        test_task_group_is_provider = {
            'id': '%s' % self.health_care_consumer_task_group.pk,
            'is_health_care_provider': False
        }
        self.assertEqual(test_task_group_is_provider, task_group_is_provider['data'],
                         'test_task_group_is_provider --> KO')

    def test_task_group_set_provider(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/set_provider/
        """
        response = self.client.post('/users/task_group/%s/set_provider/' % self.health_care_consumer_task_group.pk)
        task_group_set_provider = json.loads(response.content)
        test_task_group_set_provider = {
            'id': '%s' % self.health_care_consumer_task_group.pk,
            'is_health_care_provider': True
        }
        self.assertEqual(test_task_group_set_provider, task_group_set_provider['data'],
                         'test_task_group_set_provider --> KO')

    def test_task_group_add_user(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/add_user/(?P<user_id>\d+)/
        """
        response = self.client.post('/users/task_group/%s/add_user/%s/' %
                                    (self.service_provider_task_group.pk, self.user.pk))
        task_group_add_user = json.loads(response.content)
        test_task_group_add_user = {
            'task_group_id': '%s' % self.service_provider_task_group.pk,
            'user_id': '%s' % self.user.pk
        }
        self.assertEqual(test_task_group_add_user, task_group_add_user['data'],
                         'test_task_group_add_user --> KO')

    def test_task_group_remove_user(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/remove_user/(?P<user_id>\d+)/
        """
        response = self.client.post('/users/task_group/%s/remove_user/%s/' %
                                    (self.service_provider_task_group.pk, self.user.pk))
        task_group_remove_user = json.loads(response.content)
        test_task_group_remove_user = {
            'task_group_id': '%s' % self.service_provider_task_group.pk,
            'user_id': '%s' % self.user.pk
        }
        self.assertEqual(test_task_group_remove_user, task_group_remove_user['data'],
                         'test_task_group_remove_user --> KO')

    def test_task_group_list_users(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/list_users/
        """
        response = self.client.get('/users/task_group/%s/list_users/' % self.service_provider_task_group.pk)
        task_group_list_users = json.loads(response.content)
        test_task_group_list_users = []
        self.assertEqual(test_task_group_list_users, task_group_list_users['data'],
                         'test_task_group_list_users --> KO')

    def test_task_group_add_related_task_group(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/add_related_task_group/(?P<related_task_group_id>\d+)/
        """
        response = self.client.post('/users/task_group/%s/add_related_task_group/%s/' %
                                    (self.health_care_provider_task_group.pk, self.health_care_consumer_task_group.pk))
        task_group_add_related = json.loads(response.content)
        test_task_group_add_related = {
            'task_group_id': '%s' % self.health_care_provider_task_group.pk,
            'related_task_group_id': '%s' % self.health_care_consumer_task_group.pk
        }
        self.assertEqual(test_task_group_add_related, task_group_add_related['data'],
                         'test_task_group_add_related_task_group --> KO')

    def test_task_group_remove_related_task_group(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/remove_related_task_group/(?P<related_task_group_id>\d+)/
        """
        response = self.client.post('/users/task_group/%s/remove_related_task_group/%s/' %
                                    (self.health_care_provider_task_group.pk, self.health_care_consumer_task_group.pk))
        task_group_remove_related = json.loads(response.content)
        test_task_group_remove_related = {
            'task_group_id': '%s' % self.health_care_provider_task_group.pk,
            'related_task_group_id': '%s' % self.health_care_consumer_task_group.pk
        }
        self.assertEqual(test_task_group_remove_related, task_group_remove_related['data'],
                         'test_task_group_remove_related_task_group --> KO')

    def test_task_group_list_related_task_groups(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/list_related_task_groups/
        """
        response = self.client.get('/users/task_group/%s/list_related_task_groups/' %
                                   self.health_care_provider_task_group.pk)
        task_group_list_related = json.loads(response.content)
        test_task_group_list_related = []
        self.assertEqual(test_task_group_list_related, task_group_list_related['data'],
                         'test_task_group_list_related_task_groups --> KO')

    def test_task_group_has_clinicians(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/has_clinicians/
        """
        response = self.client.get('/users/task_group/%s/has_clinicians/' %
                                   self.health_care_provider_task_group.pk)
        task_group_has_clinicians = json.loads(response.content)
        test_task_group_has_clinicians = {
            'task_group_id': '%s' % self.health_care_provider_task_group.pk,
            'clinicians_count': 0
        }
        self.assertEqual(test_task_group_has_clinicians, task_group_has_clinicians['data'],
                         'test_task_group_has_clinicians --> KO')

    def test_task_group_list_clinicians(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/list_clinicians/
        """
        response = self.client.get('/users/task_group/%s/list_clinicians/' %
                                   self.health_care_provider_task_group.pk)
        task_group_list_clinicians = json.loads(response.content)
        test_task_group_list_clinicians = []
        self.assertEqual(test_task_group_list_clinicians, task_group_list_clinicians['data'],
                         'test_task_group_list_clinicians --> KO')

    def test_task_group_has_clinician_provider(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/has_clinician_provider/
        """
        response = self.client.get('/users/task_group/%s/has_clinician_provider/' %
                                   self.health_care_provider_task_group.pk)
        task_group_clinician_provider = json.loads(response.content)
        test_task_group_clinician_provider = {
            'task_group_id': '%s' % self.health_care_provider_task_group.pk,
            'clinicians_count': 0
        }
        self.assertEqual(test_task_group_clinician_provider, task_group_clinician_provider['data'],
                         'test_task_group_has_clinician_provider --> KO')

    def test_task_group_list_clinician_providers(self):
        """
        Test API: /users/task_group/(?P<task_group_id>\d+)/list_clinician_providers/
        """
        response = self.client.get('/users/task_group/%s/list_clinician_providers/' %
                                   self.health_care_provider_task_group.pk)
        task_group_list_clinician_provider = json.loads(response.content)
        test_task_group_list_clinician_provider = []
        self.assertEqual(test_task_group_list_clinician_provider, task_group_list_clinician_provider['data'],
                         'test_task_group_list_clinician_providers --> KO')


class MostUserAPITest(TestCase):
    """ MostUser API tests """

    def setUp(self):
        self.user = MostUser.objects.create_user(username='test', first_name='test', last_name='test',
                                                 email='test@most.crs4.it', user_type='CL', password='test')
        self.user.is_admin = True
        self.user.save()
        self.client.login(username='test', password='test')

    def test_most_user_new(self):
        """
        Test API: /users/user/new/
        """
        data = {
            'username': 'utente_test',
            'first_name': 'nome_test',
            'last_name': 'cognome_test',
            'email': 'nome.cognome@most.crs4.it',
            'birth_date': '1978-12-01',
            'is_active': True,
            'is_admin': False,
            'numeric_password': '1234',
            'user_type': 'TE',
            'gender': 'M',
            'phone': '369874521',
            'mobile': '258741369',
            'certified_email': 'nome.cognome@most.legalmail.crs4.it'
        }
        response = self.client.post('/users/user/new/', data=data)
        user = json.loads(response.content)
        test_user = {
            'username': u'%s' % data['username'],
            'first_name': u'%s' % data['first_name'],
            'last_name': u'%s' % data['last_name'],
            'birth_date': u'%s' % datetime.strptime(data['birth_date'], '%Y-%m-%d').strftime('%d %b %Y'),
            'is_staff': False,
            'is_active': data['is_active'],
            'is_admin': data['is_admin'],
            'user_type': {
                'key': u'%s' % data['user_type'],
                'value': u'%s' % 'Technician'
            },
            'gender': {
                'key': u'%s' % data['gender'],
                'value': u'%s' % 'Male'
            },
            'email': u'%s' % data['email'],
            'phone': u'%s' % data['phone'],
            'mobile': u'%s' % data['mobile'],
            'certified_email': u'%s' % data['certified_email']
        }
        user['data'].pop('id')
        user['data'].pop('uid')
        self.assertEqual(test_user, user['data'], 'test_user_new --> KO')

    def test_most_user_get_user_info(self):
        """
        Test API: /users/user/(?P<user_id>\d+)/get_user_info/
        """
        response = self.client.get('/users/user/%s/get_user_info/' % self.user.pk)
        user = json.loads(response.content)
        test_user = self.user.to_dictionary()
        self.assertEqual(test_user, user['data'], 'test_most_user_get_user_info --> KO')

    def test_most_user_search(self):
        """
        Test API: /users/user/search/
        """
        query_string = 'test'
        response = self.client.get('/users/user/search/', data={'query_string': query_string})
        users = json.loads(response.content)
        test_users = [self.user.to_dictionary()]
        self.assertEqual(test_users, users['data'], 'test_most_user_search --> KO')

    def test_most_user_edit(self):
        """
        Test API: /users/user/(?P<user_id>\d+)/edit/
        """
        data = {
            'username': self.user.username,
            'first_name': self.user.first_name,
            'last_name': self.user.last_name,
            'email': 'nome.cognome@most.crs4.it',
            'birth_date': '1985-03-12',
            'is_active': self.user.is_active,
            'is_admin': self.user.is_admin,
            'numeric_password': self.user.numeric_password,
            'user_type': self.user.user_type,
            'gender': self.user.gender,
            'certified_email': 'nome.cognome@most.legalmail.crs4.it'
        }
        test_user = self.user.to_dictionary()
        test_user['email'] = data['email']
        test_user['birth_date'] = datetime.strptime(data['birth_date'], '%Y-%m-%d').strftime('%d %b %Y')
        test_user['certified_email'] = data['certified_email']
        response = self.client.post('/users/user/%s/edit/' % self.user.pk, data=data)
        user = json.loads(response.content)
        self.assertDictEqual(test_user, user['data'], 'test_most_user_edit --> KO')

    def test_most_user_deactivate(self):
        """
        Test API: /users/user/(?P<user_id>\d+)/deactivate/
        """
        response = self.client.post('/users/user/%s/deactivate/' % self.user.pk)
        user = json.loads(response.content)
        test_user = {'id': '%s' % self.user.pk, 'is_active': False}
        self.assertDictEqual(test_user, user['data'], 'test_most_user_deactivate --> KO')

    def test_most_user_activate(self):
        """
        Test API: /users/user/(?P<user_id>\d+)/activate/
        """
        response = self.client.post('/users/user/%s/activate/' % self.user.pk)
        user = json.loads(response.content)
        test_user = {'id': '%s' % self.user.pk, 'is_active': True}
        self.assertDictEqual(test_user, user['data'], 'test_most_user_activate --> KO')


class ClinicianUserAPITest(TestCase):
    """ ClinicianUser API Test
    (r'^clinician_user/(?P<user_id>\d+)/get_user_info/$', clinician_user.get_user_info),  # get
    """

    def setUp(self):
        self.user = MostUser.objects.create_user(username='test', first_name='test', last_name='test',
                                                 email='test@most.crs4.it', user_type='CL', password='test')
        self.user.is_admin = True
        self.user.save()
        self.doctor_clinician_user = ClinicianUser.objects.create(
            user=MostUser.objects.create_user(username='doctortest', first_name='test', last_name='doctor',
                                              email='doctor@most.crs4.it', user_type='CL', password='doctor'),
            clinician_type='DR',
            specialization='Test Specialization',
            is_health_care_provider=False
        )
        self.operator_clinician_user = ClinicianUser.objects.create(
            user=MostUser.objects.create_user(username='operatortest', first_name='test', last_name='operator',
                                              email='operator@most.crs4.it', user_type='CL', password='operator'),
            clinician_type='OP'
        )
        self.client.login(username='test', password='test')

    def test_clinician_user_new(self):
        """
        Test API: /users/clinician_user/new/
        """
        data = {
            'user': self.user.pk,
            'clinician_type': 'DR',
            'specialization': 'Tester',
            'is_health_care_provider': True
        }
        response = self.client.post('/users/clinician_user/new/', data=data)
        clinician_user = json.loads(response.content)
        test_clinician_user = {
            u'clinician_type': {
                u'key': u'%s' % data['clinician_type'],
                u'value': u'%s' % 'Doctor'
            },
            u'specialization': u'%s' % data['specialization'],
            u'is_health_care_provider': data['is_health_care_provider']
        }
        self.assertDictEqual(test_clinician_user, clinician_user['data'], 'test_clinician_user_new --> KO')

    # def test_clinician_user_edit(self):
    #     """
    #     Test API: /users/clinician_user/(?P<user_id>\d+)/edit/
    #     """
    #     print self.doctor_clinician_user.user.pk
    #     data = {
    #         'user': self.doctor_clinician_user.user.pk,
    #         'clinician_type': self.doctor_clinician_user.clinician_type,
    #         'specialization': 'MOST Tester',
    #         'is_health_care_provider': self.doctor_clinician_user.is_health_care_provider
    #     }
    #     response = self.client.post('/users/clinician_user/%s/edit/' % self.doctor_clinician_user.user.pk, data=data)
    #     print response
    #     clinician_user = json.loads(response.content)
    #     test_clinician_user = {
    #         u'clinician_type': {
    #             u'key': u'%s' % data['clinician_type'],
    #             u'value': u'%s' % 'Doctor'
    #         },
    #         u'specialization': u'%s' % data['specialization'],
    #         u'is_health_care_provider': data['is_health_care_provider']
    #     }
    #     self.assertDictEqual(test_clinician_user, clinician_user['data'], 'test_clinician_user_edit --> KO')

    def test_is_clinician_user_is_provider(self):
        """
        Test API: /users/clinician_user/(?P<user_id>\d+)/is_provider/
        """
        response = self.client.get('/users/clinician_user/%s/is_provider/' % self.doctor_clinician_user.user.pk)
        is_clinician_user_provider = json.loads(response.content)
        test_is_clinician_user_provider = {
            'user_id': u'%s' % self.doctor_clinician_user.user.pk,
            'is_health_care_provider': self.doctor_clinician_user.is_health_care_provider
        }
        self.assertDictEqual(test_is_clinician_user_provider, is_clinician_user_provider['data'],
                             'test_is_clinician_user_provider --> KO')

    def test_is_clinician_user_set_provider(self):
        """
        Test API: /users/clinician_user/(?P<user_id>\d+)/set_provider/
        """
        response = self.client.post('/users/clinician_user/%s/set_provider/' % self.doctor_clinician_user.user.pk)
        clinician_user_provider = json.loads(response.content)
        test_clinician_user_provider = {
            'user_id': u'%s' % self.doctor_clinician_user.user.pk,
            'is_health_care_provider': not self.doctor_clinician_user.is_health_care_provider
        }
        self.assertDictEqual(test_clinician_user_provider, clinician_user_provider['data'],
                             'test_is_clinician_user_set_provider --> KO')

    def test_clinician_user_search(self):
        """
        Test API: /users/clinician_user/search/
        """
        query_string = 'test'
        response = self.client.get('/users/clinician_user/search/', data={'query_string': query_string})
        users = json.loads(response.content)
        test_users = [
            self.doctor_clinician_user.to_dictionary(),
            self.operator_clinician_user.to_dictionary(),
        ]
        self.assertListEqual(test_users, users['data'], 'test_clinician_user_search --> KO')

    def test_clinician_user_get_user_info(self):
        """
        Test API: /users/clinician_user/(?P<user_id>\d+)/get_user_info/
        """
        response = self.client.get('/users/clinician_user/%s/get_user_info/' % self.operator_clinician_user.user.pk)
        user_info = json.loads(response.content)
        test_user_info = self.operator_clinician_user.user.to_dictionary()
        self.assertDictEqual(test_user_info, user_info['data'], 'test_clinician_user_get_user_info --> KO')