#!/bin/bash
cd /tmp/most-demo
make devel
make sync
cd server/most && PYTHONPATH=.. python manage.py loaddata /tmp/django_initial.json