#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.conf.urls.defaults import patterns, include, url

urlpatterns = patterns('air.release.views',
    url(r'^$', 'index', name='index_url'),
    url(r'^(?P<project_name>\w+)/$', 'detail', name='detail_url' ),
    url(r'^(?P<project_name>\w+)/public/$', 'imagelist', {'category': 'p'}, name='public_url'),
    url(r'^(?P<project_name>\w+)/internal/$', 'imagelist', {'category': 'i'}, name='internal_url'),
    url(r'^(?P<project_name>\w+)/admin/$', 'imageadmin', name='admin_url'),
    url(r'^(?P<project_name>\w+)/config/$', 'projectconf', name='config_url'),
    url(r'^(?P<project_name>\w+)/maillist/$', 'mail_group_list', name='mail_list_url'),
    url(r'^(?P<project_name>\w+)/maillist/(?P<group_name>\w+)/$', 'mail_user_list', name='mail_user_list_url'),
    url(r'^(?P<project_name>\w+)/(?P<version_id>\d+)/releasenote/$', 'releasenote', name='releasenote_url'),
    url(r'^(?P<project_name>\w+)/(?P<version_pk>\d+)/releasenote/get/$', 'getreleasenotepage', name='releasenotepage_url'),
    #url(r'^(?P<project_name>\w+)/testresult/$', 'testresult', name='testresult_url'),
    url(r'^(?P<project_name>\w+)/(?P<version_id>\d+)/$', 'versionmod', name='versionmod_url'),
    url(r'^(?P<project_name>\w+)/changelog/(?P<version_pk>\d+)/$', 'changelog', name='changelog_url'),
    url(r'^(?P<project_name>\w+)/changelog/get/$', 'getchangelog', name='getchangelog_url'),
    #url(r'^(?P<project_name>\w+)/(?P<version>\w.*)/$', 'get_pdf', name='get_pdf_url'),
    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
)
