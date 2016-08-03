# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import most.web.utils
from django.conf import settings


class Migration(migrations.Migration):

    dependencies = [
        ('users', '0001_initial'),
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('streaming', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='Device',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('uuid', models.CharField(unique=True, max_length=100, verbose_name='Device UUID')),
                ('description', models.CharField(max_length=50, verbose_name='Description')),
                ('application_type', models.CharField(max_length=2, verbose_name='Application type', choices=[(b'AA', b'Applicant App'), (b'SA', b'Specialist App')])),
                ('device_platform', models.CharField(max_length=7, verbose_name='Device platform', choices=[(b'IOS', b'Apple platform'), (b'ANDROID', b'Android platform')])),
                ('task_groups', models.ManyToManyField(related_name='devices', null=True, verbose_name='Device Task Groups', to='users.TaskGroup', blank=True)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Room',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('uuid', models.CharField(default=most.web.utils.pkgen, unique=True, max_length=40)),
                ('name', models.CharField(max_length=200)),
                ('description', models.CharField(max_length=200)),
                ('has_encoder', models.BooleanField(default=True)),
                ('has_camera', models.BooleanField(default=True)),
                ('camera_device', models.ForeignKey(related_name='camera_rooms', blank=True, to='streaming.StreamingDevice', null=True)),
                ('encoder_device', models.ForeignKey(related_name='encoder_rooms', blank=True, to='streaming.StreamingDevice', null=True)),
                ('task_group', models.ForeignKey(related_name='rooms', to='users.TaskGroup')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Teleconsultation',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('uuid', models.CharField(default=most.web.utils.pkgen, unique=True, max_length=40)),
                ('description', models.CharField(max_length=200, verbose_name='Description')),
                ('state', models.CharField(max_length=20, verbose_name='Teleconsultation State', choices=[(b'NEW', b'New Teleconsultation'), (b'OPEN', b'At Least one Session Open - (SESSION WAITING STATE'), (b'ACTIVE', b'Last Session in progress'), (b'CLOSE', b'Last Session is closed')])),
                ('severity', models.CharField(default=b'NORMAL', max_length=20, verbose_name='Severity State', choices=[(b'LOW', b'Low severity'), (b'NORMAL', b'Normal severity'), (b'URGENCY', b'Urgency'), (b'EMERGENCY', b'Emergency')])),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('updated', models.DateTimeField(auto_now=True)),
                ('applicant', models.ForeignKey(related_name='has_applicant', blank=True, to=settings.AUTH_USER_MODEL, null=True)),
                ('specialist', models.ForeignKey(related_name='has_specialist', blank=True, to=settings.AUTH_USER_MODEL, null=True)),
                ('task_group', models.ForeignKey(related_name='teleconsultations', to='users.TaskGroup')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='TeleconsultationSession',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('uuid', models.CharField(default=most.web.utils.pkgen, unique=True, max_length=40)),
                ('state', models.CharField(default=b'NEW', max_length=20, verbose_name='Teleconsultation Session State', choices=[(b'NEW', b'New Session'), (b'WAITING', b'Session waiting for specialist'), (b'READY', b'Session ready to start'), (b'RUN', b'Session in progress'), (b'CLOSE', b'Session is closed'), (b'CANCELED', b'Session is canceled')])),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('updated', models.DateTimeField(auto_now=True)),
                ('spec_app_address', models.CharField(default=b'', max_length=15)),
                ('room', models.ForeignKey(related_name='sessions', to='teleconsultation.Room')),
                ('teleconsultation', models.ForeignKey(related_name='sessions', to='teleconsultation.Teleconsultation')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
    ]
