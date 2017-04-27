#!/bin/bash
cd /tmp/most-demo
make devel
make sync
cd server/most && PYTHONPATH=.. python manage.py loaddata /tmp/most-demo/server/docker/django_initial.json