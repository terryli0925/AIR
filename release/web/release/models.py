#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.db import models
from django.contrib.auth.models import Group
import os.path

class Stage(models.Model):
    name = models.CharField(max_length=10)

    def __unicode__(self):
        return self.name

class BaselineType(models.Model):
    bstype = models.CharField(max_length=10)

    def __unicode__(self):
        return self.bstype

class Project(models.Model):
    name = models.CharField(max_length=20, unique=True)
    platform = models.CharField(max_length=100)
    sku = models.IntegerField()
    baseline = models.CharField(max_length=20)
    stage = models.ForeignKey(Stage)
    baselinetp = models.ForeignKey(BaselineType)
    permission = models.ManyToManyField(Group)

    # For ASD1 FTP Uploading
    ftp_username = models.CharField(max_length=50, null=True, blank=True)
    ftp_password = models.CharField(max_length=50, null=True, blank=True)
    ftp_host = models.CharField(max_length=50, null=True, blank=True)
    ftp_upload_path = models.CharField(max_length=200, null=True, blank=True)
    ftp_folder_have_project_name = models.BooleanField(default=True)
    ftp_folder_have_version_name = models.BooleanField(default=True)

    def __unicode__(self):
        return self.name

    class Meta:
        permissions = [
            ('view_public', 'Can see public image'),
            ('view_internal', 'Can see internal image'),
            ('view_admin', 'Can see admin page'),
        ]

class MailGroup(models.Model):
    name = models.CharField(max_length=20, unique=True)
    status = models.CharField(max_length=1) # This is same as version status
    type = models.CharField(max_length=5) # TO/BCC/CC
    project = models.ManyToManyField(Project)

    def __unicode__(self):
        return self.name

class MailUser(models.Model):
    name = models.CharField(max_length=20, unique=True)
    email = models.EmailField(unique=True)
    groups = models.ManyToManyField(MailGroup)

    def __unicode__(self):
        return self.name

class BaselineInclusion(models.Model):
    name = models.CharField(max_length=30)
    version = models.CharField(max_length=30)
    desc = models.CharField(max_length=200)
    project = models.ForeignKey(Project)

class FirmwareCatalog(models.Model):
    name = models.CharField(max_length=20)

    def __unicode__(self):
        return self.name

class ReleaseInfoCatalog(models.Model):
    name = models.CharField(max_length=30)
    
    def __unicode__(self):
        return self.name

class Comment(models.Model):
    comment = models.TextField()

class ImageFile(models.Model):
    filedata = models.FileField(upload_to='images/')
    checksum = models.CharField(max_length=100)
    checksumtype = models.CharField(max_length=10)
    
    def __unicode__(self):
        return os.path.basename(self.filedata.file.name)

class ReleaseNoteFile(models.Model):
    filedata = models.FileField(upload_to='notes/')

    def __unicode__(self):
        return os.path.basename(self.filedata.file.name)

class Version(models.Model):
    name = models.CharField(max_length=30)
    project = models.ForeignKey(Project)
    comments = models.OneToOneField(Comment, blank=True, null=True)
    #P: Public, I:Internal, N:None(Every Image start from this status), D: Deleted
    status = models.CharField(max_length=1, default='N')
    filename = models.OneToOneField(ImageFile)
    releasename = models.CharField(max_length=30, blank=True, null=True)
    releasenote = models.OneToOneField(ReleaseNoteFile, blank=True, null=True)
    releasedate = models.DateField(blank=True, null=True)

    def __unicode__(self):
        return self.name

class Firmware(models.Model):
    version = models.CharField(max_length=20)
    catalog = models.ForeignKey(FirmwareCatalog)
    project = models.ForeignKey(Project)
    release_version = models.ManyToManyField(Version)

    def __unicode__(self):
        return self.version

class Changelog(models.Model):
    key = models.CharField(max_length=200, unique=True)
    author = models.CharField(max_length=100)
    author_mail = models.EmailField()
    date = models.DateTimeField()
    subject = models.CharField(max_length=200)
    content = models.TextField()
    version = models.ManyToManyField(Version, blank=True, null=True)

    def __unicode__(self):
        return self.key

class ReleaseInfo(models.Model):
    catalog = models.ForeignKey(ReleaseInfoCatalog)
    content = models.CharField(max_length=200)
    version = models.ForeignKey(Version)

    def __unicode__(self):
        return self.content

