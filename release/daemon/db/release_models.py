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
from django.contrib.auth.models import Group
import os.path


class Project(models.StandaloneModel):
    name = models.CharField(max_length=20, unique=True)
    
    class Meta:
        db_table = 'release_project'

    def __unicode__(self):
        return self.name

class MailGroup(models.StandaloneModel):
    name = models.CharField(max_length=20, unique=True)
    status = models.CharField(max_length=1) # This is same as version status
    type = models.CharField(max_length=5) # TO/BCC/CC
    project = models.ManyToManyField(Project)

    class Meta:
        db_table = 'release_mailgroup'

    def __unicode__(self):
        return self.name

class MailUser(models.StandaloneModel):
    name = models.CharField(max_length=20, unique=True)
    email = models.EmailField(unique=True)
    groups = models.ManyToManyField(MailGroup)

    class Meta:
        db_table = 'release_mailuser'

    def __unicode__(self):
        return self.name

class ImageFile(models.StandaloneModel):
    filedata = models.FileField(upload_to='images/')
    checksum = models.CharField(max_length=100)
    checksumtype = models.CharField(max_length=10)

    class Meta:
        db_table = 'release_imagefile'

    def __unicode__(self):
        return os.path.basename(self.filedata.file.name)

class Version(models.StandaloneModel):
    name = models.CharField(max_length=30)
    project = models.ForeignKey(Project)
    filename = models.OneToOneField(ImageFile)
    status = models.CharField(max_length=1, default='N')
    releasename = models.CharField(max_length=30, blank=True, null=True)

    class Meta:
        db_table = 'release_version'

    def __unicode__(self):
        return self.name

class Changelog(models.StandaloneModel):
    key = models.CharField(max_length=200, unique=True)
    author = models.CharField(max_length=100)
    author_mail = models.EmailField()
    date = models.DateTimeField()
    subject = models.CharField(max_length=200)
    content = models.TextField()
    version = models.ManyToManyField(Version, blank=True, null=True)

    class Meta:
        db_table = 'release_changelog'

    def __unicode__(self):
        return self.key

