#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.db import models
from release.models import Project, Version

class TestDomain(models.Model):
    name = models.CharField(max_length=200)
    order = models.IntegerField()

    def __unicode__(self):
        return self.name

class TestFunction(models.Model):
    name = models.CharField(max_length=200)
    domain = models.ForeignKey(TestDomain)
    order = models.IntegerField()

    def __unicode__(self):
        return self.name

class TestItemParameterName(models.Model):
    name = models.CharField(max_length=200)

    def __unicode__(self):
        return self.name

class TestItem(models.Model):
    name = models.CharField(max_length=200)
    function = models.ForeignKey(TestFunction)
    is_autotest = models.BooleanField(default=True)
    order = models.IntegerField()
    parameter_name = models.ManyToManyField(TestItemParameterName, blank=True, null=True)

    def __unicode__(self):
        return self.name

class TestConfig(models.Model):
    project = models.ForeignKey(Project)
    config = models.FileField(upload_to='config/')

    def __unicode__(self):
        return self.config.path

class TestResult(models.Model):
    version = models.ForeignKey(Version)
    result = models.FileField(upload_to='result/')
    result_log = models.FileField(upload_to='result/')
    result_manual = models.FileField(upload_to='result/', blank=True, null=True)
    client = models.CharField(max_length=50)
    create_date = models.DateTimeField(auto_now_add=True)
    modify_date = models.DateTimeField(auto_now=True)
    test_item = models.IntegerField()
    test_pass = models.IntegerField()
    test_failed = models.IntegerField()

    def __unicode__(self):
        return self.version