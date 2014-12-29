#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.conf.urls.defaults import patterns, include, url

urlpatterns = patterns('air.testcase.views',
    url(r'^$', 'index', name='index_url'),
    url(r'^(?P<project_name>\w+)/$', 'detail', name='detail_url'),
    url(r'^(?P<project_name>\w+)/config/$', 'config', name='config_url' ),
    url(r'^(?P<project_name>\w+)/config/save/$', 'save_config', name='save_config_url' ),
    url(r'^(?P<project_name>\w+)/result/list/$', 'list_result', name='list_result_url' ),
    url(r'^(?P<project_name>\w+)/result/(?P<version>\w.*)/(?P<result_id>\d+)/$', 'get_result', name='get_result_url'),
    url(r'^(?P<project_name>\w+)/result/(?P<version>\w.*)/(?P<result_id>\d+)/edit/$', 'edit_result', name='edit_result_url'),
    url(r'^(?P<project_name>\w+)/result/(?P<version>\w.*)/(?P<result_id>\d+)/edit/save/$', 'save_result', name='save_result_url'),
    url(r'^(?P<project_name>\w+)/result/(?P<version>\w.*)/$', 'get_result', name='get_result_url'),

)
