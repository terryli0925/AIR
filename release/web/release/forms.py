#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django import forms
from django.forms import widgets, ModelForm
from release.models import *
from release.widgets import BootstrapInlineRadioRenderer
from datetime import date

class ProjectConfigForm(forms.Form):
    name = forms.CharField(label='Project Code')
    platform = forms.CharField(label='Platform')
    sku = forms.CharField(label='SKU')
    stage = forms.ModelChoiceField(
            label='Stage', 
            widget=widgets.RadioSelect(renderer=BootstrapInlineRadioRenderer), 
            queryset=Stage.objects.all(), 
            empty_label=None)

    ftp_username = forms.CharField(label='FTP Username',max_length=50, required=False)
    ftp_password = forms.CharField(label='FTP Password',max_length=50, required=False, widget=widgets.PasswordInput(render_value=True))
    ftp_host = forms.CharField(label='FTP Host',max_length=50, required=False)
    ftp_upload_path = forms.CharField(label='Upload Path',max_length=50, required=False)
    ftp_folder_have_project_name = forms.BooleanField(label='Folder include project name', required=False)
    ftp_folder_have_version_name = forms.BooleanField(label='Folder include version name', required=False)

class ReleaseNoteForm(forms.Form):
    v_pk = forms.CharField(widget=forms.HiddenInput)
    name = forms.CharField(label='Project Code')
    platform = forms.CharField(label='Platform')
    sku = forms.CharField(label='SKU')
    stage = forms.ModelChoiceField(
            label='Stage', 
            widget=widgets.RadioSelect(renderer=BootstrapInlineRadioRenderer), 
            queryset=Stage.objects.all(), 
            empty_label=None)

    baseline = forms.CharField(label='Baseline Version',max_length=20)
    bstype = forms.ModelChoiceField(
            label='Baseline Type',
            widget=widgets.RadioSelect(renderer=BootstrapInlineRadioRenderer),
            queryset=BaselineType.objects.all(),
            empty_label=None)

    releasedate = forms.DateField(label='Release Date', initial=date.today(), input_formats=('%Y-%m-%d',), widget=widgets.DateInput(format='%Y-%m-%d'))
    releasename = forms.CharField(label='Release Name / Version')
    checksum = forms.CharField(label='Checksum', widget=widgets.TextInput(attrs={'class': 'span4'}))
    comments = forms.CharField(label='', widget=forms.Textarea(attrs={'rows':4}), required=False)

class FirmwareRequirementForm(ModelForm):
    class Meta:
        model = Firmware
        exclude = ('project', 'release_version')
        widgets = {
            'catalog': widgets.Select(attrs={'class': 'span2'}),
            'version': widgets.TextInput(attrs={'class': 'span4'})
        }

class BaselineForm(ModelForm):
    class Meta:
        model = BaselineInclusion
        exclude = ('project')
        widgets = {
            'name': widgets.TextInput(attrs={'class': 'span2'}),
            'version': widgets.TextInput(attrs={'class': 'span2'}),
            'desc': widgets.TextInput(attrs={'class': 'span4'})
        }

class ChangelogForm(ModelForm):
    catalog = forms.ModelChoiceField(
            label = '',
            widget = widgets.Select(attrs={'class': 'span2'}),
            queryset = ReleaseInfoCatalog.objects.all(),
            empty_label=None
            )

    class Meta:
        model = Changelog
        fields = {'subject'}
        widgets = {
            'subject': widgets.TextInput(attrs={'class': 'span8'})
        }

class ReleaseInfoForm(ModelForm):
    class Meta:
        model = ReleaseInfo
        exclude = ('version')
        widgets = {
            'catalog': widgets.Select(attrs={'class': 'span2'}),
            'content': widgets.TextInput(attrs={'class': 'span8'})
        }

class ChangelogFilter(forms.Form):
    filter_from = forms.ModelChoiceField(label='', queryset=Version.objects.none(), empty_label=None)
    filter_to = forms.ModelChoiceField(label='', queryset=Version.objects.none(), empty_label=None)

class VersionModForm(forms.Form):
    version = forms.CharField(label='Release Version Name', widget=widgets.TextInput(attrs={'class': 'span4'}))
    confirm = forms.BooleanField(label='Confirm to Release?')
