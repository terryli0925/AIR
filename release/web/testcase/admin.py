#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from testcase.models import *
from django.contrib import admin

class TestItemInline(admin.StackedInline):
    model = TestItem
    extra = 3

class TestFunctionAdmin(admin.ModelAdmin):
    fieldsets = [
        ('Function Name',       {'fields': ['name']}),
        ('Function Domain',     {'fields': ['domain']})
    ]
    inlines = [TestItemInline]

class TestFunctionInline(admin.StackedInline):
    model = TestFunction
    extra = 1

class TestDomainAdmin(admin.ModelAdmin):
    fieldsets = [
        ('Domain Name',         {'fields': ['name']}),
        ('Domain Order',        {'fields': ['order']}),
    ]
    inlines = [TestFunctionInline]

admin.site.register(TestDomain, TestDomainAdmin)
admin.site.register(TestFunction, TestFunctionAdmin)
admin.site.register(TestItem)
admin.site.register(TestItemParameterName)
admin.site.register(TestConfig)
admin.site.register(TestResult)