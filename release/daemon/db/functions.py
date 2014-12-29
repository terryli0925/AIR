#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

import settings
import os, dateutil.parser, hashlib, tempfile, shutil, datetime
import smtplib
from lxml import etree
from release_models import *
from testcase_models import *
from django.core.files.base import File
from email.mime.text import MIMEText

# Calculate MD5 Hash
def get_file_md5_hash(file):
    md5_hash = hashlib.md5()
    print 'Hashing File'
    for bytes in read_bytes_from_file(file):
        md5_hash.update(bytes)

    return md5_hash.hexdigest()

# Read bytes from a file in chuck
def read_bytes_from_file(file, chuck_size = 8100):
    with open(file, 'rb') as file:
        while True:
            chuck = file.read(chuck_size)
            if chuck:
                yield chuck
            else:
                break;

# Add Version To DB
def add_version(version, project, file_data):
    print 'Enter Database Processing Phase'
    print file_data
    upload = File(open(file_data['path'], 'rb'))

    try:
        v = Version.objects.get(name=version)
        file_path = os.path.join(settings.MEDIA_ROOT, v.filename.filedata.path)
        # Prepare old pk for later delete
        old_file_id = v.filename.pk
        # Remove old file in storage
        if os.path.exists(file_path):
            os.remove(file_path)
        # Upload new image file
        f = ImageFile(filedata=upload, checksum=file_data['hash'], checksumtype='MD5')
        f.save()
        # Update version image file info
        v.filename = f
        v.save()
        # Remove old file in table
        ImageFile.objects.get(pk=old_file_id).delete()
    except Version.DoesNotExist:
        print 'New Version'
        try:
            # Upload new image file
            f = ImageFile(filedata=upload, checksum=file_data['hash'], checksumtype='MD5')
            f.save()
            p = Project.objects.get(name=project)
            v = Version(name=version, project=p, filename=f)
            v.releasename = ''
            v.save()
        except Project.DoesNotExist:
            print 'Project Error'
            return False

    return True

# Parse Changelog
def add_changelog(version, file_data):
    try:
        v = Version.objects.get(name=version)
        logfile = open(file_data, 'rb')
        log = logfile.read()
        log = log.split('|')

        print 'Parsing Changelog'

        for l in log:
            if l.lstrip(os.linesep).startswith('KEY='):
                key = l.rpartition('=')[2]
            elif l.lstrip(os.linesep).startswith('AUTHOR='):
                author = l.rpartition('=')[2]
            elif l.lstrip(os.linesep).startswith('EMAIL='):
                email = l.rpartition('=')[2]
            elif l.lstrip(os.linesep).startswith('DATE='):
                date = dateutil.parser.parse(l.rpartition('=')[2], ignoretz=True)
            elif l.lstrip(os.linesep).startswith('SUBJECT='):
                subject = l.rpartition('=')[2]
            elif l.lstrip(os.linesep).startswith('BODY='):
                body = l.rpartition('=')[2]

                try:
                    c = Changelog.objects.get(key=key)
                    print 'Changelog %s Already Exist' % key
                    c.version.add(v)
                    c.save()
                except Changelog.DoesNotExist:
                    print 'New Changelog %s' % key
                    c = Changelog(key=key, author=author, author_mail=email, date=date, subject=subject, content=body)
                    c.save()
                    c.version.add(v)
                    c.save()

    except Version.DoesNotExist:
        print 'Version Does Not Exist'
        return False

    return True

# Return TestItemConfig.xml from DB
def get_config_xml(project, path):
    xml = ''
    try:
        p = Project.objects.get(name=project)
        c = TestConfig.objects.get(project=p)
        xml = os.path.join(settings.MEDIA_ROOT, c.config.path)
    except TestConfig.DoesNotExist:
        print 'No Config File'
        xml = os.path.join(settings.MEDIA_ROOT, 'config/default.xml')
    except Project.DoesNotExist:
        print 'Project Error'
    shutil.copy(xml, path)
    return dict(type='CONFIG_XML', name=xml.rpartition('/')[2].encode('utf-8'), path=os.path.join(path, xml.rpartition('/')[2]), hash=get_file_md5_hash(xml))

# Return Image File from DB
def get_image_file(version, path):
    img = ''
    try:
        v = Version.objects.get(name=version)
        img = os.path.join(settings.MEDIA_ROOT, v.filename.filedata.path)
        filename = img.rpartition('/')[2].encode('utf-8')
        shutil.copy(img, path)
        img = os.path.join(path, filename)
        return dict(type='IMG', name=filename, path=img, hash=v.filename.checksum.encode('utf-8'))
    except Version.DoesNotExist:
        print 'Version Error'
    return None

def parse_result(project, version, client, file, md5, xml, log):

    counter_tests = 0 # Total Auto Test Item
    counter_pass = 0 # Pass Item
    counter_failed = 0 # Failed Item

    ##### Parse Test Result XML #####
    result_xml = open(xml, 'rb')
    parser = etree.XMLParser(recover=True) # Recover=True -> Fix broken xml
    result_tree = etree.parse(result_xml, parser)
    result_xml.close()
    result_dict = {}

    # Parse TestItem to dict for DB phase query
    for e in result_tree.getiterator('TestItem'):
        try:
            e_id = e.find('ID').text
            e_result = e.find('Pass').text

            if e_result == 'True':
                e_result = 'Pass'
                counter_pass += 1
            elif e_result == 'False':
                e_result = 'Failed'
                counter_failed += 1

            e_log = e.find('Log').text
            e_remark = []
            for remark in e.getiterator():
                if remark.tag.startswith('Remark'):
                    if remark.text:
                        e_remark.append(remark.text)

            #print 'ID:%s R:%s L:%s RM:%s' % (e_id, e_result, e_log, e_remark)
            # If there have same item id, ignore it
            if not result_dict.has_key(e_id):
                result_dict[e_id] = {'Result': e_result, 'Log': e_log, 'Remark': e_remark}
                counter_tests += 1

        except AttributeError:
            # This is caused by incomplete XML tag
            print 'Broken Item'

    ##### End of Parse Test Result XML #####

    ##### Generate XML from DB #####
    current_domain = '' # save current domain for generating XML
    current_function = '' # save current function for generating XML

    # Create General Info to XML
    result_xml = etree.Element('TestResult', Project=project)
    etree.SubElement(result_xml, 'Client').text = client
    etree.SubElement(result_xml, 'Create').text = datetime.datetime.now().strftime('%Y/%m/%d %H:%M')
    etree.SubElement(result_xml, 'Modify').text = datetime.datetime.now().strftime('%Y/%m/%d %H:%M')
    etree.SubElement(result_xml, 'Version').text = version
    etree.SubElement(result_xml, 'File').text = file
    etree.SubElement(result_xml, 'Checksum').text = md5
    tree_tests = etree.Element('Tests')
    # Set test info to attr
    tree_tests.set('Total', '%s' % counter_tests)
    tree_tests.set('Pass', '%s' % counter_pass)
    tree_tests.set('Failed', '%s' % counter_failed)

    result_xml.append(tree_tests)

    for d in TestDomain.objects.all(): # Query all test domain
        domain = d.name
        tree_domain = etree.Element('Domain', name=domain) # Create new domain node
        tree_tests.append(tree_domain)
        for f in TestFunction.objects.filter(domain=d):
            function = f.name
            db_items = TestItem.objects.filter(function=f, is_autotest=True)
            if db_items:
                tree_function = etree.Element('Function', name=function) # Create new function node
                tree_domain.append(tree_function)
                for i in db_items:
                    # Append test item to function
                    tree_item = etree.SubElement(tree_function, 'Item', name=i.name, id='%s' % i.id)
                    tree_result = etree.SubElement(tree_item, 'Result') # Create result node

                    if result_dict.has_key('%s' % i.id): # If there is an id in result_dict, get the result
                        tree_result.text = result_dict['%s' % i.id]['Result']
                        etree.SubElement(tree_item, 'Log').text = result_dict['%s' % i.id]['Log']

                        for r in result_dict['%s' % i.id]['Remark']:
                            etree.SubElement(tree_item, 'Remark').text = r
                    else:
                        tree_result.text = 'N/A'
        if not tree_domain.getchildren():
            tree_tests.remove(tree_domain)

    ##### End of Generate From DB #####

    ##### Save XML to DB #####
    try:
        print 'DB Processing'
        db_version = Version.objects.get(name=version)
        #try: # If there is already have the result file, override it
        #    db_result = TestResult.objects.get(version=db_version)
        #    db_result.test_item = counter_tests
        #    db_result.test_pass = counter_pass
        #    db_result.test_failed = counter_failed
        #    tmp_file = open(db_result.result.path, 'wb')
        #    etree.ElementTree(result_xml).write(tmp_file, pretty_print=True)
        #    tmp_file.close()
        #    db_result.save()
        #except TestResult.DoesNotExist: # If not, create temp file and save to DB
        db_result = TestResult(version=db_version, client=client, test_item=counter_tests, test_pass=counter_pass, test_failed=counter_failed)
        tmp_file = tempfile.NamedTemporaryFile()
        log_file = open(log, 'rb+')
        etree.ElementTree(result_xml).write(tmp_file, pretty_print=True)
        f = File(tmp_file)
        l_f = File(log_file)
        db_result.result.save('%s_TestResult_%s_%s.xml' % (version, client, datetime.datetime.now().strftime('%Y%m%d%H%M')), f, save=False)
        db_result.result_log.save('%s_TestLog_%s_%s.zip' % (version, client, datetime.datetime.now().strftime('%Y%m%d%H%M')), l_f)
        tmp_file.close()
    except Version.DoesNotExist: # If the version not exist, ignore it
        print 'Version %s Does Not Exist' % version
        return False

    return True

def send_mail(project, version, status):
    project = Project.objects.get(name=project)
    version = Version.objects.get(name=version)
    result = TestResult.objects.filter(version=version).order_by('-id')[0]

    sender = 'AIR Server'
    to = []
    cc = []
    bcc = []

    for g in MailGroup.objects.filter(project=project, status='N'):
        if g.type == 'TO':
            for u in MailUser.objects.filter(groups=g):
                to.append(u.email)
        elif g.type == 'CC':
            for u in MailUser.objects.filter(groups=g):
                cc.append(u.email)
        elif g.type == 'BCC':
            for u in MailUser.objects.filter(groups=g):
                bcc.append(u.email)

    text = 'Dear,\n\n[%s] has been tested, you can go to http://10.109.39.139:8010/release/%s/admin/ for detail,\n\nTest Item: %s\n\nPass: %s\n\nFailed: %s' % (
        version, project.name, result.test_item, result.test_pass, result.test_failed
        )

    if status == 'TIMEOUT':
        text = 'Dear,\n\n[%s] test is TIMEOUT, you can go to http://10.109.39.139:8010/release/%s/admin/ for detail,\n\nTest Item: %s\n\nPass: %s\n\nFailed: %s' % (
            version, project.name, result.test_item, result.test_pass, result.test_failed
            )

    msg = MIMEText(text)
    msg['Subject'] = '[%s] Test finished and uploaded' % version

    if status == 'TIMEOUT':
        msg['Subject'] = '!!![%s] Test timeout' % version

    msg['From'] = sender
    msg['To'] = ','.join(to)
    msg['Cc'] = ','.join(cc)
    msg['Bcc'] = ','.join(bcc)

    receiver = to + cc + bcc

    s = smtplib.SMTP('10.109.39.139')
    try:
        s.sendmail(sender, receiver, msg.as_string())
    except smtplib.SMTPRecipientsRefused:
        print 'SMTP Recipients Error, is that empty?'
        print 'TO:%s' % to
        print 'CC:%s' % cc
        print 'BCC:%s' % bcc
    s.quit()