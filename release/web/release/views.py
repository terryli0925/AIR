#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

import os, tempfile, logging
from release.models import *
from release.forms import *
from testcase.models import *
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.shortcuts import render, get_object_or_404, redirect
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger
from django.core.mail import EmailMessage, EmailMultiAlternatives
from django.core.urlresolvers import reverse
from django.core.files.base import File
from django.contrib.sites.models import Site
from django.contrib.auth.models import User
from django.template.loader import render_to_string
from django.forms.models import inlineformset_factory, modelformset_factory
from django.conf import settings
from django_xhtml2pdf.utils import generate_pdf
from django.contrib.auth.decorators import login_required, permission_required

@login_required(login_url='/accounts/login/')
def index(request):
    try:
        user = request.user
        project = Project.objects.filter(permission__in=user.groups.all())[0]
    except IndexError:
        return HttpResponse('<body>Your have no access permission to any project</body>')
    return redirect('/release/' + project.name + '/public/')

@login_required(login_url='/accounts/login/')
def detail(request, project_name):
    return redirect('/release/' + project_name + '/public/')

@permission_required('release.view_public')
def imagelist(request, project_name, category):
    if category == 'i' and not request.user.has_perm('release.view_internal'):
        return HttpResponse('<body>Permission Denied</body>')

    user = request.user
    project_list = Project.objects.filter(permission__in=user.groups.all())

    if not project_list:
        return HttpResponse('<body>Your have no access permission to any project</body>')

    try:
        project = Project.objects.get(name=project_name)
        if not project in project_list:
            return redirect('release:public_url', project_name=project_list[0].name)
    except Project.DoesNotExist:
        return redirect('release:public_url', project_name=project_list[0].name)

    version_list = []

    for v in Version.objects.filter(status=category, project=project).order_by('-id'):
        try:
            r = TestResult.objects.filter(version=v).order_by('-id')[0]
            version_list.append(dict(version=v,result=r))
        except TestResult.DoesNotExist:
            version_list.append(dict(version=v,result=None))
        except IndexError:
            version_list.append(dict(version=v,result=None))

    paginator = Paginator(version_list, 5)

    page = request.GET.get('page', 1)
    try:
        versions = paginator.page(page)
    except PageNotAnInteger:
        versions = paginator.page(1)
    except EmptyPage:
        versions = paginator.page(paginator.num_pages)

    return render(request, 'release/project_imagelist.html', {'projectlist': project_list, 'project': project, 'versionlist': versions})

@permission_required('release.view_admin')
def imageadmin(request, project_name):
    project_list = Project.objects.all()
    try:
        project = Project.objects.get(name=project_name)
    except:
        raise Http404

    version_list = []

    for v in Version.objects.filter(project=project).order_by('-id'):
        try:
            r = TestResult.objects.filter(version=v).order_by('-id')[0]
            version_list.append((v, r))
        except TestResult.DoesNotExist:
            version_list.append((v,None))
        except IndexError:
            version_list.append((v,None))

    return render(request, 'release/project_imageadmin.html', {'projectlist': project_list, 'project': project, 'versionlist': version_list})

@permission_required('release.view_admin')
def projectconf(request, project_name):
    project_list = Project.objects.all()
    project = Project.objects.get(name=project_name)
    
    if request.method == 'POST':
        form = ProjectConfigForm(request.POST)
        if form.is_valid():
            project.name = form.cleaned_data['name']
            project.platform = form.cleaned_data['platform']
            project.sku = form.cleaned_data['sku']
            project.stage = form.cleaned_data['stage']
            project.ftp_username = form.cleaned_data['ftp_username']
            project.ftp_password = form.cleaned_data['ftp_password']
            project.ftp_host = form.cleaned_data['ftp_host']
            project.ftp_upload_path = form.cleaned_data['ftp_upload_path']
            project.ftp_folder_have_project_name = form.cleaned_data['ftp_folder_have_project_name']
            project.ftp_folder_have_version_name = form.cleaned_data['ftp_folder_have_version_name']
            project.save()
            return HttpResponseRedirect(reverse('release:config_url', args=[project_name]))
    else:
        if project:
            form = ProjectConfigForm(initial={
                'name': project.name,
                'platform': project.platform,
                'sku': project.sku,
                'stage': project.stage,
                'ftp_username': project.ftp_username,
                'ftp_password': project.ftp_password,
                'ftp_host': project.ftp_host,
                'ftp_upload_path': project.ftp_upload_path,
                'ftp_folder_have_project_name': project.ftp_folder_have_project_name,
                'ftp_folder_have_version_name': project.ftp_folder_have_version_name})
    return render(request, 'release/project_config.html', {'form':form, 'projectlist': project_list, 'project': project})

@permission_required('release.view_admin')
def releasenote(request, project_name, version_id):
    project = get_object_or_404(Project, name=project_name)
    version = get_object_or_404(Version, pk=version_id)
    project_list = Project.objects.all()
    
    FirmwareFormset = inlineformset_factory(Project, Firmware, fk_name='project', extra=2, form=FirmwareRequirementForm)
    BaselineFormset = inlineformset_factory(Project, BaselineInclusion, extra=2, form=BaselineForm)
    ReleaseInfoFormset = inlineformset_factory(Version, ReleaseInfo, fk_name='version', extra=0, form=ReleaseInfoForm)
    ChangelogFormset = modelformset_factory(Changelog, extra=0, can_delete=True, form=ChangelogForm)
    
    releaseinfoform = ReleaseInfoFormset(instance=version, prefix='releaseinfo')

    # Get Version List for Changelog from last Public to edit version 
    # Should be integrated to changelogfilterform
    if version.status == 'P':
        versionlist = Version.objects.filter(pk = version.pk)
    else:
        try:
            lastpublicversion = Version.objects.filter(project=project, status='P', pk__lte=version.pk).order_by('-pk')[0]
            versionlist = Version.objects.filter(project=project, pk__gt=lastpublicversion.pk, pk__lte=version.pk)
        except IndexError:
            versionlist = Version.objects.filter(project=project, pk__lte=version.pk)

    if request.method == 'POST':
        basicform = ReleaseNoteForm(request.POST, prefix='basic')
        firmwareform = FirmwareFormset(request.POST, request.FILES, instance=project, prefix='firmware')
        baselineform = BaselineFormset(request.POST, request.FILES, instance=project, prefix='baseline')

        #Fix Bug#1105: If release note file didn't exist but already have release info
        releaseinfo = ReleaseInfo.objects.filter(version=version)[:1]
        if releaseinfo:
            releaseinfoform = ReleaseInfoFormset(request.POST, request.FILES, instance=version, prefix='releaseinfo')
        else:
            changelogfilter = ChangelogFilter(request.POST, prefix='filter')
            changelogfilter.fields['filter_from'].queryset = versionlist.order_by('pk')
            changelogfilter.fields['filter_to'].queryset = versionlist.order_by('-pk')
            changelogset = ChangelogFormset(request.POST, prefix='changelog')

        if basicform.is_valid() and firmwareform.is_valid() and baselineform.is_valid():
            basicdata = basicform.cleaned_data
            project.platform = basicdata['platform']
            project.sku = basicdata['sku']
            project.stage = basicdata['stage']
            project.baseline = basicdata['baseline']
            project.baselinetp = basicdata['bstype']
            version.releasename = basicdata['releasename']
            version.releasedate = basicdata['releasedate']
            version.filename.checksum = basicdata['checksum']
            
            if releaseinfo:
                if releaseinfoform.is_valid():
                    releaseinfoform.save()
            else:
                if changelogset.is_valid():
                    for c in changelogset:
                        data = c.cleaned_data
                        if not data['DELETE']:
                            ri = ReleaseInfo(catalog=data['catalog'], content=data['subject'], version=version)
                            ri.save()

            project.save()
            version.save()
            firmwareform.save()
            baselineform.save()
            return HttpResponseRedirect(reverse('release:releasenotepage_url', args=[project_name, version_id]))
        else:
            return HttpResponseRedirect(reverse('release:releasenote_url', args=[project_name, version_id]))
    else:
        if version.releasename != '':
            releasename = version.releasename
        else:
            releasename = version.name
        
        firmwareform = FirmwareFormset(instance=project, prefix='firmware')
        baselineform = BaselineFormset(instance=project, prefix='baseline')

        basicform = ReleaseNoteForm(initial={
            'v_pk': version_id, 
            'name': project.name,
            'platform': project.platform,
            'sku': project.sku,
            'stage': project.stage,
            'baseline': project.baseline,
            'bstype': project.baselinetp,
            'releasename': releasename,
            'checksum': version.filename.checksum,
            'comments': version.comments}, prefix='basic')
        
        changelogfilter = ChangelogFilter(prefix='filter')
        changelogfilter.fields['filter_from'].queryset = versionlist.order_by('pk')
        changelogfilter.fields['filter_to'].queryset = versionlist.order_by('-pk')

    return render(request, 'release/project_releasenote.html', {
        'projectlist': project_list,
        'project': project,
        'version': version,
        'basicform': basicform,
        'firmwareform': firmwareform,
        'baselineform': baselineform,
        'changelogfilter': changelogfilter,
        'releaseinfoform': releaseinfoform})

#@permission_required('release.view_public')
#def testresult(request, project_name):
#    project_list = Project.objects.all()
#    project = Project.objects.get(name=project_name)
#    return render(request, 'release/project_testresult.html', {'projectlist': project_list, 'project': project})

@permission_required('release.view_admin')
def changelog(request, project_name, version_pk):
    project_list = Project.objects.all()
    project = Project.objects.get(name=project_name)
    version = Version.objects.get(pk=version_pk)
    changelog = Changelog.objects.filter(version=version_pk)
    return render(request, 'release/project_changelog.html', {'projectlist': project_list, 'project': project, 'changelog': changelog, 'version': version})

@permission_required('release.view_admin')
def mail_group_list(request, project_name):
    project = get_object_or_404(Project, name=project_name)
    try:
        group = MailGroup.objects.filter(project=project)[0]
        return redirect('release:mail_user_list_url', project_name=project.name, group_name=group)
    except IndexError:
        return redirect('release:public_url', project_name=project.name)

@permission_required('release.view_admin')
def mail_user_list(request, project_name, group_name):
    project_list = Project.objects.all()
    project = Project.objects.get(name=project_name)
    group_list = MailGroup.objects.filter(project=project)
    group = get_object_or_404(MailGroup, name=group_name)
    selected_users = MailUser.objects.filter(groups=group)
    unselected_users = MailUser.objects.exclude(groups=group)

    if request.method == 'POST':
        status = request.POST.get('status', 'None')
        type = request.POST.get('type', 'None')
        users = request.POST.getlist('receivers')
        # Save mail group setting
        if status:
            group.status = status
        if type:
            group.type = type
        group.save()
        # Add selected user to group
        db_users = MailUser.objects.filter(id__in = users)
        for u in db_users:
            u.groups.add(group)
            u.save()
        # Remove unselected from group
        for u in selected_users:
            if not u in db_users:
                u.groups.remove(group)
                u.save()
        return HttpResponseRedirect(reverse('release:mail_user_list_url', args=[project_name, group_name]))

    return render(request, 'release/project_mail_user_list.html', {'projectlist': project_list,
                                                                   'project': project,
                                                                   'group_list': group_list,
                                                                   'group': group,
                                                                   'selected_users': selected_users,
                                                                   'unselected_users': unselected_users})

@permission_required('release.view_admin')
def versionmod(request, project_name, version_id):  
    project = Project.objects.get(name=project_name)
    version = Version.objects.get(pk=version_id)
    
    if 'fn' in request.GET:
        fn = request.GET['fn']

        if fn == 'N':
            title = 'Image Recycle'
        elif fn == 'I':
            title = 'Internal Release'
        elif fn == 'P':
            title = 'Public Release'
        elif fn == 'D':
            title = 'Delete Image'
        
        if version.releasename:
            initname = version.releasename
        else:
            initname = version.name

        versionmodform = VersionModForm(initial={'version': initname})
        
        return render(request, 'release/version_mod.html', {
            'project': project, 
            'title': title, 
            'version': version, 
            'fn': fn, 
            'form': versionmodform })


    if request.method == 'POST':
        fn = request.POST.get('fn', 'N')
        versionmodform = VersionModForm(request.POST)

        if versionmodform.is_valid():
            data = versionmodform.cleaned_data
            if data['confirm']:
                version.releasename = data['version']
                version.status = fn
                version.save()
                if fn == 'I' or fn == 'P':
                    sendreleasemail(project=project, version=version)
                if fn == 'P' and project.ftp_username and project.ftp_password and project.ftp_host and project.ftp_upload_path:
                    upload_ftp(project=project, version=version)
        
        return HttpResponseRedirect(reverse('release:admin_url', args=[project_name]))
    
    return redirect('/release/' + project_name + '/admin/')

@permission_required('release.view_public')
def getchangelog(request, project_name):
    if request.method == 'POST':
        project = get_object_or_404(Project, name=project_name)
        versionlist = Version.objects.filter(project=project, pk__range=(request.POST.get('filterfrom'), request.POST.get('filterto')))
        ChangelogFormset = modelformset_factory(Changelog, extra=0, can_delete=True, form=ChangelogForm)
        changelogset = ChangelogFormset(queryset=Changelog.objects.filter(version__in=versionlist), prefix='changelog')

        return render(request, 'release/release_getchangelog.html', {'changelogset': changelogset})
    
    raise Http404

@permission_required('release.view_admin')
def getreleasenotepage(request, project_name, version_pk):
    project_list = Project.objects.all()
    project = get_object_or_404(Project, name=project_name)
    version = get_object_or_404(Version, pk=version_pk)

    stage = Stage.objects.all()
    baselinetype = BaselineType.objects.all()
    firmwarecatalog = FirmwareCatalog.objects.all()
    releaseinfocatalog = ReleaseInfoCatalog.objects.all()

    baselineinclusion = BaselineInclusion.objects.filter(project=project)
    firmware = Firmware.objects.filter(project=project)

    releaseinfo = []

    for rc in releaseinfocatalog:
        releaseinfo.append(ReleaseInfo.objects.filter(version=version, catalog=rc))
    
    # Non-ASCii Code character
    checkbox = '\xe2\x96\xa1'
    filledcheckbox = '\xe2\x96\xa0'
    
    if version.releasenote:
        tmpfile = open(version.releasenote.filedata.path, 'w+')
        result = generate_pdf('release/release_pdf.html', file_object=tmpfile, context=locals())
        tmpfile.close()

    else:
        filename = ''.join('%s_SDP_2_1_08_01_a_Release_Note_Ver_%s.pdf' % (project.name, version.releasename))

        # XHTML2PDF
        tmpfile = tempfile.NamedTemporaryFile()
        result = generate_pdf('release/release_pdf.html', file_object=tmpfile, context=locals())
        f = File(tmpfile)
        # For Database
        rnfile = ReleaseNoteFile()
        rnfile.filedata.save(filename , f)
        tmpfile.close()

        version.releasenote = rnfile
        version.save()

    response = HttpResponse(version.releasenote.filedata.read(), mimetype='application/pdf')
    return response

##### General Function #####
def sendreleasemail(project=None, version=None):
    if project and version:
        to = []
        cc = []
        bcc = []

        # Prepare Mail Content
        template_text = 'mail/plain_release.txt'
        attach = None
        site = Site.objects.get_current()
        subject = 'No Subject'

        if version.status == 'I':
            subject = u'[%s] SW Image Internal Release %s' % (project.name, version.releasename)
            for g in MailGroup.objects.filter(project=project, status='I'):
                if g.type == 'TO':
                    for u in MailUser.objects.filter(groups=g):
                        to.append(u.email)
                elif g.type == 'CC':
                    for u in MailUser.objects.filter(groups=g):
                        cc.append(u.email)
                elif g.type == 'BCC':
                    for u in MailUser.objects.filter(groups=g):
                        bcc.append(u.email)

        elif version.status == 'P':
            subject = u'[%s] SW Image Release %s' % (project.name, version.releasename)
            for g in MailGroup.objects.filter(project=project, status='P'):
                if g.type == 'TO':
                    for u in MailUser.objects.filter(groups=g):
                        to.append(u.email)
                elif g.type == 'CC':
                    for u in MailUser.objects.filter(groups=g):
                        cc.append(u.email)
                elif g.type == 'BCC':
                    for u in MailUser.objects.filter(groups=g):
                        bcc.append(u.email)

            if version.releasenote:
                attach = version.releasenote.filedata

        content = render_to_string(template_text, {'site': site, 'version': version})
        mail = EmailMessage(subject, content, 'air@compal.com', to=to, bcc=bcc, cc=cc)
        if attach:
            mail.attach(attach.name, attach.read(), 'application/pdf')
        mail.send()

def upload_ftp(project=None, version=None):
    if project and version:
        import ftplib
        ftp = ftplib.FTP()
        ftp.connect(project.ftp_host, '21')
        ftp.login(project.ftp_username, project.ftp_password)
        upload_path = project.ftp_upload_path.split('/')
        for path in upload_path:
            try:
                ftp.cwd(path)
            except Exception:
                ftp.mkd(path)
                ftp.cwd(path)
        if project.ftp_folder_have_project_name:
            try:
                ftp.cwd(project.name)
            except Exception:
                ftp.mkd(project.name)
                ftp.cwd(project.name)
        if project.ftp_folder_have_version_name:
            try:
                if version.releasename:
                    ftp.cwd(version.releasename)
                else:
                    ftp.cwd(version.name)
            except Exception:
                if version.releasename:
                    ftp.mkd(version.releasename)
                    ftp.cwd(version.releasename)
                else:
                    ftp.mkd(version.name)
                    ftp.mkd(version.name)
        img_path = os.path.join(settings.MEDIA_ROOT, version.filename.filedata.path)
        f = open(img_path, 'rb+')
        ftp.storbinary('STOR %s' % version.filename.filedata.path.rpartition('/')[2], f)
        f.close()
        ftp.quit()