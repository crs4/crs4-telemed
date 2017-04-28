#!/bin/bash
cd /opt/crs4-telemed
make devel
make sync
cd server/most && PYTHONPATH=.. python manage.py loaddata /opt/crs4-telemed/server/docker/crs4-telemed/django_initial.json