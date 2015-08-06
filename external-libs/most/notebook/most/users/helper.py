#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

import requests
import json

USERNAME = 'valeria'
PASSWORD = 'valleria'

HOST_ADDRESS = 'http://127.0.0.1:8000'
USER_LOGGED_IN = False

s = requests.session()


def print_response_data(class_name, dictionary):
    print u'JSON response: %s\n' % dictionary
    if dictionary['success']:
        # dump dictionary to print human readable data
        if 'data' in dictionary:
            print u'%s data:\n%s' % (class_name, json.dumps(dictionary['data'], sort_keys=True, indent=4,
                                                            separators=(',', ': ')))
        else:
            print u'data:\n%s' % json.dumps(dictionary, sort_keys=True, indent=4, separators=(',', ': '))
    else:
        print dictionary['errors']


def compose_post_request(api, data=None):
    print 'Calling %s\n' % api
    if data:
        response = s.post('%s%s' % (HOST_ADDRESS, api), data=data)
    else:
        response = s.post('%s%s' % (HOST_ADDRESS, api))
    # print response.text
    return response.json()


def compose_get_request(api, params=None):
    print 'Calling %s\n' % api
    if params:
        response = s.get('%s%s' % (HOST_ADDRESS, api), params={'query_string': params})
    else:
        response = s.get('%s%s' % (HOST_ADDRESS, api))
    return response.json()