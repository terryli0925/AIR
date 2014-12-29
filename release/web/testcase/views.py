#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from django.core.urlresolvers import reverse
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.shortcuts import render, get_object_or_404, redirect
from django.core.files.base import File
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger
from django.contrib.auth.decorators import login_required, permission_required
from release.models import Project, Version, ImageFile
from testcase.models import *
from lxml import etree
import tempfile, datetime

@login_required(login_url='/accounts/login/')
def index(request):
    try:
        user = request.user
        project = Project.objects.filter(permission__in=user.groups.all())[0]
    except IndexError:
        return HttpResponse('<body>Your have no access permission to any project</body>')
    return redirect('/test/' + project.name + '/result/list/')

@login_required(login_url='/accounts/login/')
def detail(request, project_name):
    return redirect('/test/' + project_name + '/result/list/')

@permission_required('release.view_admin')
def config(request, project_name):
    project_list = Project.objects.all()
    project = get_object_or_404(Project, name=project_name)
    selected_default = True
    config_dict = {}
    try:
        test_config = TestConfig.objects.get(project=project)
        config_dict = parse_config(test_config)
        selected_default = False
    except TestConfig.DoesNotExist:
        pass

    test_domain = TestDomain.objects.all().order_by('order')
    domain_list = []
    for d in test_domain:
        functions = []
        counter = 0
        for f in TestFunction.objects.filter(domain=d).order_by('order'):
            items = []
            i_counter = 0
            selected = selected_default
            for i in TestItem.objects.filter(function=f, is_autotest=True).order_by('order'):
                # If we config the item and it has been selected, ignore this
                if not selected_default and not selected:
                    if config_dict.has_key('%s' % i.pk):
                        if config_dict['%s' % i.pk]['selected'] == 'True':
                            selected = True
                parameter = []
                i_counter += 1
                for idx, p in enumerate(i.parameter_name.iterator()):
                    value = None
                    if config_dict.items():
                        try:
                            value=config_dict['%s' % i.pk]['parameter'][idx]
                        except IndexError:
                            pass
                    parameter.append(dict(parameter=p, value=value))
                items.append(dict(item=i, parameter=parameter))
            if items:
                functions.append(dict(function=f, items=items, count=i_counter, select=selected))
                counter += 1
                counter += i_counter
        if functions:
            domain_list.append(dict(domain=d, functions=functions, count=counter))

    return render(request, 'testcase/testcase_config.html',
            {'project_list': project_list,
             'project': project,
             'domain_list': domain_list
        })

@permission_required('release.view_admin')
def save_config(request, project_name):
    if request.method == 'POST':
        post_case = request.POST.getlist('testcase')
        root = etree.Element('configuration')
        test_domain = TestDomain.objects.all().order_by('order')
        for d in test_domain:
            for f in TestFunction.objects.filter(domain=d).order_by('order'):
                for i in TestItem.objects.filter(function=f, is_autotest=True).order_by('order'):
                    child = etree.Element('TestItem', domain=d.name, type=f.name, description=i.name)
                    root.append(child)
                    child2 = etree.SubElement(child, 'Check')
                    if post_case.count(f.name) != 0:
                        child2.text = 'True'
                    else:
                        child2.text = 'False'

                    child3 = etree.SubElement(child, 'ID')
                    child3.text = '%s' % i.pk

                    for idx,p in enumerate(i.parameter_name.all()):
                        if p:
                            child4 = etree.SubElement(child, 'Remark%s' % (idx+1))
                            child4.text = request.POST.get('%s_%s' % (i.pk,p.name), '')

        xmlstr = etree.tostring(root, pretty_print=True)

        # Prepare DB & Query old xml data
        project = get_object_or_404(Project, name=project_name)
        try: #If there is already have data, just overwrite it
            test_config = TestConfig.objects.get(project=project)
            tmpfile = open(test_config.config.path, 'wb')
            etree.ElementTree(root).write(tmpfile, pretty_print=True)
            tmpfile.close()
        except: #Or make a temp file and save to DB
            test_config = TestConfig(project=project)
            tmpfile = tempfile.NamedTemporaryFile()
            etree.ElementTree(root).write(tmpfile, pretty_print=True)
            f = File(tmpfile)
            test_config.config.save('%s_TestItemConfig.xml' % project.name, f)
            tmpfile.close()

        #return HttpResponse('<body>%s</body>' % post_case)
        return HttpResponseRedirect(reverse('testcase:list_result_url', args=[project_name]))
    else:
        return HttpResponse("YOU CAN'T TOUCH THIS")

@permission_required('release.view_public')
def list_result(request, project_name):
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

    #version_list = Version.objects.filter(project=project)
    version_list = []
    #result_list = TestResult.objects.filter(version__in=version_list).order_by('-id')
    for v in Version.objects.filter(project=project).order_by('-id'):
        result_list = []

        for r in TestResult.objects.filter(version=v).order_by('-id'):
            result_list.append(r)

        if result_list:
            version_list.append(dict(version=v, results=result_list))

    return render(request, 'testcase/testcase_resultlist.html', {'project_list': project_list, 'project': project, 'versionlist': version_list})

@permission_required('release.view_public')
def get_result(request, project_name, version, result_id=None):
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

    version = get_object_or_404(Version, name=version)
    try:
        if result_id:
            result = TestResult.objects.get(id=result_id)
        else:
            result = TestResult.objects.filter(version=version).order_by('-id')[0]
    except TestResult.DoesNotExist:
        return Http404()

    result_xml = open(result.result.path, 'rb+')
    parser = etree.XMLParser(recover=True)
    result_tree = etree.parse(result_xml, parser)
    result_xml.close()

    result_list = parse_result(result_tree)

    try:
        filename = result_tree.find('File').text
        checksum = result_tree.find('Checksum').text

    except AttributeError:
        pass

    result_manual_list = None
    if result.result_manual:
        result_manual_xml = open(result.result_manual.path, 'rb+')
        result_manual_tree = etree.parse(result_manual_xml, parser)
        result_manual_xml.close()

        result_manual_list = parse_result(result_manual_tree)

    return render(request, 'testcase/testcase_result.html', {'project': version.project,
                                                             'version': version,
                                                             'project_list': project_list,
                                                             'result_list': result_list,
                                                             'result_manual_list': result_manual_list,
                                                             'result': result,
                                                             'filename': filename,
                                                             'checksum': checksum})

@permission_required('release.view_admin')
def edit_result(request, project_name, version, result_id=None):
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

    version = get_object_or_404(Version, name=version)
    try:
        if result_id:
            result = TestResult.objects.get(id=result_id)
        else:
            result = TestResult.objects.filter(version=version).order_by('-id')[0]
    except TestResult.DoesNotExist:
        return Http404()

    result_xml = open(result.result.path, 'rb+')
    parser = etree.XMLParser(recover=True)
    result_tree = etree.parse(result_xml, parser)
    result_xml.close()

    try:
        filename = result_tree.find('File').text
        checksum = result_tree.find('Checksum').text
    except AttributeError:
        pass

    result_list = parse_result(result_tree)
    result_manual_list = []

    if result.result_manual:
        result_manual_xml = open(result.result_manual.path, 'rb+')
        result_manual_tree = etree.parse(result_manual_xml, parser)
        result_manual_xml.close()
        result_manual_list = parse_result(result_manual_tree)
    else:
        db_items = TestItem.objects.filter(is_autotest=False)

        for itm in db_items:
            domain = itm.function.domain.name
            domain_find = False
            function = itm.function.name
            function_find = False
            item = dict(name=itm.name.encode('utf-8'), id='%s' % itm.id, result='N/A')
            for d in result_manual_list:
                if d['name'] == domain:
                    domain_find = True
                    for f in d['functions']:
                        if f['name'] == function:
                            function_find = True
                            f['items'].append(item)
                            f['count'] += 1
                    if not function_find:
                        f = dict(name=function, items=[], count=0)
                        f['items'].append(item)
                        f['count'] += 1
                        d['functions'].append(f)
                    else:
                        i_counter = 0
                        for f in d['functions']:
                          i_counter += f['count']
                        d['count'] = i_counter + len(d['functions'])
            if not domain_find:
                d = dict(name=domain, functions=[], count=0)
                f = dict(name=function, items=[], count=0)
                f['items'].append(item)
                f['count'] += 1
                d['functions'].append(f)
                d['count'] = len(d['functions']) + 1
                result_manual_list.append(d)

    return render(request, 'testcase/testcase_result_edit.html', {'project': version.project,
                                                             'version': version,
                                                             'project_list': project_list,
                                                             'result_list': result_list,
                                                             'result_manual_list': result_manual_list,
                                                             'result': result,
                                                             'filename': filename,
                                                             'checksum': checksum})

@permission_required('release.view_admin')
def save_result(request, project_name, version, result_id):
    if request.method == 'POST':
        result = get_object_or_404(TestResult, id=result_id)
        auto_result_list = {}
        manual_result_list = {}
        auto_counter_pass = 0
        auto_counter_failed = 0
        manual_counter_total = 0
        manual_counter_pass = 0
        manual_counter_failed = 0

        for name, var in request.POST.items():
            if name.startswith('auto_test'):
                auto_result_list['%s' % name.split('-')[1]] = var
                if var == 'Pass':
                    auto_counter_pass += 1
                elif var == 'Failed':
                    auto_counter_failed += 1
            elif name.startswith('manual_test'):
                manual_result_list['%s' % name.split('-')[1]] = var
                if var == 'Pass':
                    manual_counter_pass += 1
                elif var == 'Failed':
                    manual_counter_failed += 1

        # Auto Test Result File
        result_xml = open(result.result.path, 'rb+')
        parser = etree.XMLParser(recover=True)
        result_tree = etree.parse(result_xml, parser)
        result_xml.close()

        result_tree.find('Modify').text = datetime.datetime.now().strftime('%Y/%m/%d %H:%M')

        auto_test_tree = result_tree.find('Tests')
        auto_test_tree.set('Pass', '%s' % auto_counter_pass)
        auto_test_tree.set('Failed', '%s' % auto_counter_failed)

        for child in result_tree.getiterator('Item'):
            id = child.get('id')
            child.find('Result').text = auto_result_list[id]

        result_xml = open(result.result.path, 'wb+')
        result_tree.write(result_xml)
        result_xml.close()
        # End of Auto Test Result File Edit

        # Manual Test Result File
        result_manual_tree = None
        if result.result_manual:
            result_manual_xml = open(result.result_manual.path, 'rb+')
            result_manual_tree = etree.parse(result_manual_xml, parser)
            result_manual_xml.close()

        if result_manual_tree:
            result_manual_tree.find('Modify').text = datetime.datetime.now().strftime('%Y/%m/%d %H:%M')
            for child in result_manual_tree.getiterator('Item'):
                id = child.get('id')
                child.find('Result').text = manual_result_list[id]

            result_manual_xml = open(result.result_manual.path, 'wb+')
            result_manual_tree.write(result_manual_xml)
            result_manual_xml.close()
        else:
            user = request.user
            version = Version.objects.get(name=version)
            image_file = ImageFile.objects.get(version=version)
            result_manual_xml = etree.Element('ManualTestResult', Project=project_name)
            etree.SubElement(result_manual_xml, 'User').text = user.username
            etree.SubElement(result_manual_xml, 'Create').text = datetime.datetime.now().strftime('%Y/%m/%d %H:%M')
            etree.SubElement(result_manual_xml, 'Modify').text = datetime.datetime.now().strftime('%Y/%m/%d %H:%M')
            etree.SubElement(result_manual_xml, 'Version').text = version.name
            etree.SubElement(result_manual_xml, 'File').text = image_file.filedata.path.rsplit('/')[0]
            etree.SubElement(result_manual_xml, 'Checksum').text = image_file.checksum
            tree_tests = etree.Element('Tests')
            # Set test info to attr
            tree_tests.set('Pass', '%s' % manual_counter_pass)
            tree_tests.set('Failed', '%s' % manual_counter_failed)

            result_manual_xml.append(tree_tests)

            for d in TestDomain.objects.all(): # Query all test domain
                domain = d.name
                tree_domain = etree.Element('Domain', name=domain) # Create new domain node
                tree_tests.append(tree_domain)
                for f in TestFunction.objects.filter(domain=d):
                    function = f.name
                    db_items = TestItem.objects.filter(function=f, is_autotest=False)
                    if db_items:
                        tree_function = etree.Element('Function', name=function) # Create new function node
                        tree_domain.append(tree_function)
                        manual_counter_total += len(db_items)
                        for i in db_items:
                            # Append test item to function
                            tree_item = etree.SubElement(tree_function, 'Item', name=i.name, id='%s' % i.id)
                            tree_result = etree.SubElement(tree_item, 'Result') # Create result node

                            if manual_result_list.has_key('%s' % i.id): # If there is an id in result_dict, get the result
                                tree_result.text = manual_result_list['%s' % i.id]
                            else:
                                tree_result.text = 'N/A'

                if not tree_domain.getchildren():
                    tree_tests.remove(tree_domain)

            tree_tests.set('Total', '%s' % manual_counter_total)
            tmp_file = tempfile.NamedTemporaryFile()
            etree.ElementTree(result_manual_xml).write(tmp_file, pretty_print=True)
            f = File(tmp_file)
            result.result_manual.save('%s_ManualTestItemResult_%s_%s.xml' % (version, user.username, datetime.datetime.now().strftime('%Y%m%d%H%M')), f)
            tmp_file.close()
        # End of Manual Test Result

        result.test_item += manual_counter_total
        result.test_pass = auto_counter_pass + manual_counter_pass
        result.test_failed = auto_counter_failed + manual_counter_failed
        result.save()

        return HttpResponseRedirect(reverse('testcase:get_result_url', args=[project_name, version, result_id]))
    else:
        return Http404

##### Parse Test Config XML #####
def parse_config(test_config):
    config_dict = {}
    config_xml = open(test_config.config.path, 'rb+')
    parser = etree.XMLParser(recover=True)
    config_tree = etree.parse(config_xml, parser)
    config_xml.close()

    # Parse TestItem to dict for DB phase query
    for e in config_tree.getiterator('TestItem'):
        try:
            e_selected = e.find('Check').text
            e_id = e.find('ID').text
            e_remark = []
            for remark in e.getiterator():
                if remark.tag.startswith('Remark'):
                    if remark.text:
                        e_remark.append(remark.text)
            config_dict[e_id] = dict(selected=e_selected, parameter=e_remark)
        except AttributeError:
            # This is caused by incomplete XML tag
            print 'Broken Item'

    return config_dict
##### End of Parse Test Config XML #####

##### Parse Test Result XML #####
def parse_result(result_tree):
    result_list = []
    try:
        tests_tree = result_tree.find('Tests')
        for d in tests_tree.getiterator('Domain'):
            domain = d.get('name')
            functions = []
            counter = 0
            for f in d.getiterator('Function'):
                function = f.get('name')
                items = []
                counter += 1
                i_counter = 0
                for i in f.getiterator('Item'):
                    item = i.get('name')
                    id = i.get('id')
                    r = i.find('Result').text
                    items.append({'name': item, 'id': id, 'result': r})
                    i_counter += 1
                functions.append({'name': function, 'items': items, 'count': i_counter})
                counter += i_counter
            result_list.append({'name': domain, 'functions': functions, 'count': counter})

    except AttributeError:
        pass

    return result_list
##### End of Parse Test Result XML #####