#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.conf.urls.defaults import patterns, include, url

urlpatterns = patterns('air.build.views',
    url(r'^$', 'index', name='index_url'),
)