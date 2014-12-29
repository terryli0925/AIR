#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

import settings
from standalone import models
from release_models import Project, Version
import os.path

class TestConfig(models.StandaloneModel):
    id = models.IntegerField(primary_key=True)
    project = models.ForeignKey(Project)
    config = models.FileField(upload_to='config/')

    class Meta:
        db_table = u'testcase_testconfig'

class TestDomain(models.StandaloneModel):
    id = models.IntegerField(primary_key=True)
    name = models.CharField(max_length=600)
    order = models.IntegerField()

    class Meta:
        db_table = u'testcase_testdomain'

class TestFunction(models.StandaloneModel):
    id = models.IntegerField(primary_key=True)
    name = models.CharField(max_length=600)
    domain = models.ForeignKey(TestDomain)
    order = models.IntegerField()

    class Meta:
        db_table = u'testcase_testfunction'

class TestItem(models.StandaloneModel):
    id = models.IntegerField(primary_key=True)
    name = models.CharField(max_length=600)
    function = models.ForeignKey(TestFunction)
    is_autotest = models.IntegerField()
    order = models.IntegerField()

    class Meta:
        db_table = u'testcase_testitem'

class TestResult(models.StandaloneModel):
    id = models.IntegerField(primary_key=True)
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

    class Meta:
        db_table = u'testcase_testresult'
