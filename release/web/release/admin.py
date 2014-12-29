#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from release.models import *
from django.contrib import admin

admin.site.register(Stage)
admin.site.register(Project)
admin.site.register(BaselineType)
admin.site.register(BaselineInclusion)
admin.site.register(FirmwareCatalog)
admin.site.register(ReleaseInfoCatalog)
admin.site.register(Comment)
admin.site.register(ImageFile)
admin.site.register(ReleaseNoteFile)
admin.site.register(ReleaseInfo)
admin.site.register(Version)
admin.site.register(Firmware)
admin.site.register(Changelog)
admin.site.register(MailGroup)
admin.site.register(MailUser)
