# -*- coding: utf-8 -*-

#
# Project MOST - Moving Outcomes to Standard Telemedicine Practice
# http://most.crs4.it/
#
# Copyright 2014, CRS4 srl. (http://www.crs4.it/)
# Dual licensed under the MIT or GPL Version 2 licenses.
# See license-GPLv2.txt or license-MIT.txt

from django import forms
from django.contrib import admin
from django.contrib.auth.models import Group
from django.contrib.auth.admin import UserAdmin
from django.contrib.auth.forms import ReadOnlyPasswordHashField
from models import ClinicianUser, MostUser, TaskGroup


class UserCreationForm(forms.ModelForm):
    password1 = forms.CharField(label='Password', widget=forms.PasswordInput)
    password2 = forms.CharField(label='Password confirmation', widget=forms.PasswordInput)

    class Meta:
        model = MostUser
        fields = ('username', 'first_name', 'last_name', 'email', 'user_type')

    def clean_password2(self):
        password1 = self.cleaned_data.get('password1')
        password2 = self.cleaned_data.get('password2')
        if password1 and password2 and password1 != password2:
            raise forms.ValidationError('Passwords don\'t match')
        return password2

    def save(self, commit=True):
        user = super(UserCreationForm, self).save(commit=False)
        user.set_password(self.cleaned_data['password1'])
        if commit:
            user.save()
        return user


class UserChangeForm(forms.ModelForm):
    password = ReadOnlyPasswordHashField

    class Meta:
        model = MostUser
        fields = (
            'username',
            'first_name',
            'last_name',
            'birth_date',
            'email',
            'numeric_password',
            'user_type',
            'gender',
            'phone',
            'mobile',
            'certified_email',
            'is_active',
            'is_admin'
        )

    def clean_password(self):
        return self.initial["password"]


class MostUserAdmin(UserAdmin):
    form = UserChangeForm
    add_form = UserCreationForm

    list_display = ('username', 'first_name', 'last_name', 'email', 'user_type', 'is_active', 'is_admin')
    list_filter = ('is_admin', 'is_active', 'user_type')
    fieldsets = (
        (None, {'fields': ('password', 'numeric_password')}),
        ('Personal info', {'fields': ('first_name', 'last_name', 'birth_date', 'gender')}),
        (None, {'fields': ('email', 'certified_email', 'phone', 'mobile', )}),
        ('Permissions', {'fields': ('is_admin', 'is_active', 'user_type')}),
    )

    add_fieldsets = ((None, {
        'classes': ('wide',),
        'fields': ('username', 'first_name', 'last_name', 'email', 'user_type', 'password1', 'password2')}
    ),)
    search_fields = ('username', 'first_name', 'last_name', 'email',)
    ordering = ('last_name', 'is_active', )
    filter_horizontal = ()

admin.site.register(MostUser, MostUserAdmin)
admin.site.unregister(Group)
admin.site.register(ClinicianUser)
admin.site.register(TaskGroup)