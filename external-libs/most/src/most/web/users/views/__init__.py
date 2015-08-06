#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

SUCCESS_KEY = 'success'
MESSAGE_KEY = 'message'
ERRORS_KEY = 'errors'
DATA_KEY = 'data'
TOTAL_KEY = 'total_count'


def staff_check(user):
    if user.is_staff:
        return True
    else:
        return False