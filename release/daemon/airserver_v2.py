#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from twisted.internet.protocol import Factory
from twisted.protocols.basic import LineReceiver
from twisted.internet import reactor
# Http Server
from twisted.web.server import Site
from twisted.web.static import File as TFile

from db.functions import *
import os, argparse, time, hashlib, subprocess

class AirServer(LineReceiver):

    def __init__(self, builders, devices, file_list, file_path):
        # Shared Info
        self.builders = builders
        self.devices = devices
        self.receiver = []

        # Client Info
        self.name = None
        self.project = None
        self.version = None
        self.role = None

        # File Received Info
        self.file_list = file_list
        self.file_path = file_path
        self.file_temp = None
        self.send_list = []
        self.current_file = None
        self.file_fail_count = 0

        # Waiting to reg client
        self.state = 'REG_CONNECTION'

        self.DEBUG = False

    def connectionMade(self):
        self.sendLine('AIR Server')

    def connectionLost(self, reason):
        if self.devices.has_key(self.name):
            del self.devices[self.name]
        elif self.builders.has_key(self.name):
            del self.builders[self.name]

        print '%d devices total' % (len(self.devices))
        print '%d builders total' % (len(self.builders))
        print 'File list %s' % self.file_list

    def lineReceived(self, line):
        print 'Line Received: %s' % line
        print 'State: %s' % self.state
        if self.state == 'REG_CONNECTION':
            self.handle_REG_CONNECTION(line)
        elif self.state == 'WAIT_COMMAND':
            self.handle_WAIT_COMMAND(line)
        ## Get File
        elif self.state == 'GET_FILE_INFO':
            self.handle_GET_FILE_INFO(line)
        elif self.state == 'SET_VERSION':
            self.change_version(line.split(':')[1])
        ## Send File
        elif self.state == 'SEND_FILE_INFO':
            self.handle_SEND_FILE_INFO(line)
        elif self.state == 'SEND_FILE':
            self.handle_SEND_FILE(line)
        ## End of Send File

    def handle_REG_CONNECTION(self, line):
        role = line.split('|')[0]
        name = line.split('|')[1]
        project = line.split('|')[2]

        self.role = role
        self.name = name
        self.project = project

        if role == 'BUILDER':
            if self.builders.has_key(name):
                self.sendLine('You are already connected')
                self.transport.lostConnection()
                return

            print 'Builder %s Connected, Responsible for %s' % (self.name, self.project)
            self.builders[name] = self
            self.version = line.split('|')[3]
        elif role == 'DEVICE':
            if self.devices.has_key(name):
                self.sendLine('You are already connected')
                self.transport.lostConnection()
                return

            print 'Device %s Connected, Responsible for %s' % (self.name, self.project)
            self.devices[name] = self

        print '%d devices total' % (len(self.devices))
        print '%d builders total' % (len(self.builders))
        self.state = 'WAIT_COMMAND'

    def handle_WAIT_COMMAND(self, line):
        if line == 'GET_FILE_INFO':
            self.state = 'GET_FILE_INFO'
            self.sendLine('READY')
        elif line == 'GET_FILE':
            self.download_file()
        elif line == 'SAVE_VERSION':
            self.state = 'SAVE_VERSION'
            self.save_version()
        elif line == 'SET_VERSION':
            self.state = 'SET_VERSION'
            self.sendLine('READY')
        elif line == 'SEND_FILE_INFO':
            self.state = 'SEND_FILE_INFO'
        elif line == 'SAVE_RESULT':
            self.save_result()
        elif line == 'SEND_SUCCESS_MAIL':
            send_mail(self.project, self.version, 'SUCCESS')
            self.sendLine('MAIL_OK')
        elif line == 'SEND_TIMEOUT_MAIL':
            send_mail(self.project, self.version, 'TIMEOUT')
            self.sendLine('MAIL_OK')

    ##### Get File Functions #####
    def handle_GET_FILE_INFO(self, line):
        if line.startswith('HASH'):
            data = line.split(':')
            # 0:HASH COMMAND, 1: File Type, 2:File Name, 3:File HASH
            file_type = data[1] # IMG, LOG, RESULT_XML, RESULT_LOG
            file_name = data[2]
            file_hash = data[3]
            file_path = os.path.join(self.file_path, file_name)
            self.current_type = file_type
            self.file_temp = {'type': file_type, 'name': file_name, 'hash': file_hash, 'path': file_path}
            print 'GET FILE INFO: %s' % self.file_temp
            self.sendLine('READY')
            self.state = 'WAIT_COMMAND'
        else:
            print 'State: %s, Command: %s' % (self.state, line)

    ##### End of Get File Functions #####

    ##### Send File Related Functions #####
    def handle_SEND_FILE_INFO(self, line):
        if line == 'READY':
            if not self.current_file:
                self.current_file = self.send_list.pop()
            print 'Sending File Info %s' % self.current_file['name']
            self.send_to_device('HASH:%s:%s:%s' % (self.current_file['type'], self.current_file['name'], self.current_file['hash']), self.project)
            self.state = 'SEND_FILE'

    def handle_SEND_FILE(self, line):
        if line == 'FILE_OK':
            print '%s Send Success' % self.current_file['name']
            if self.send_list:
                self.current_file = self.send_list.pop()
                self.state = 'SEND_FILE_INFO'
                self.sendLine('GET_FILE_INFO')
            else:
                print 'All file send, waiting for test'
                self.sendLine('START_TEST')
                self.state = 'WAIT_COMMAND'
        else:
            self.sendLine('GET_FILE')
            print 'Please download file from http://%s:%s/' % (self.transport.getHost().host, '8092')

    ##### End of Send File Functions #####

    ##### DB Access Functions #####
    def save_version(self):
        if self.DEBUG:
            print '[Fake Version Saved]'
            self.sendLine('VERSION_OK')
        else:
            if add_version(self.version, self.project, self.factory.file_list[self.version]['IMG']):
                print '[Version Saved]'

                if add_changelog(self.version, self.factory.file_list[self.version]['LOG']['path']):
                    print '[Changelog parsed]'
                    self.sendLine('VERSION_OK')

        self.state = 'WAIT_COMMAND'
        self.send_to_device('GET_VERSION_INFO', self.project)
        self.send_to_device('VERSION:%s' % self.version , self.project)
        self.send_to_device('GET_FILE_INFO', self.project)

    def save_result(self):
        version = self.version
        img_name = self.factory.file_list[version]['IMG']['name']
        img_md5 = self.factory.file_list[version]['IMG']['hash']
        if parse_result(self.project, version, self.name, img_name, img_md5, self.factory.file_list[version]['RESULT_XML']['path'], self.factory.file_list[version]['RESULT_LOG']['path']):
            print 'Parse Complete'
            self.sendLine('RESULT_OK')
            self.deleteFile(self.version)
        else:
            print 'Parse Failed'
            self.sendLine('RESULT_FAILED')
        self.state = 'WAIT_COMMAND'

    ##### End of DB Access Functions #####

    ##### General Functions #####
    def deleteFile(self, version):
        for i, f in self.file_list[version].iteritems():
            print 'Removing %s' % f['name']
            os.remove(f['path'])
        del self.file_list[version]

    def change_version(self, version):
        self.version = version
        config = get_config_xml(self.project, self.file_path)
        self.send_list = []
        self.current_file = None
        if not self.factory.file_list.has_key(version):
            self.file_list[version] = {}
            if not self.factory.file_list[version].has_key('IMG'):
                self.factory.file_list[version]['IMG'] = get_image_file(self.version, self.file_path)
        self.send_list.append(self.factory.file_list[version]['IMG'])
        self.send_list.append(config)
        self.file_list[self.version]['CONFIG_XML'] = config # This is for final remove
        self.state = 'WAIT_COMMAND'
        self.sendLine('GET_FILE_INFO')
        print self.send_list

    def send_to_device(self, command, project):
        for name, protocol in self.devices.iteritems():
            if protocol.project == project:
                print 'Send command %s to %s' % (command, name)
                protocol.sendLine(command)

    def download_file(self):
        # Client Port
        port = 8092
        if self.role == 'DEVICE':
            port = 8093

        file_name = self.file_temp['name']
        file_hash = self.file_temp['hash']
        print 'Download file %s' % file_name
        subprocess.check_call(['curl', '-L',
                               'http://%s:%s/%s' % (self.transport.getPeer().host, port, file_name),
                               '-o', '%s/%s' % (self.file_path, file_name),
                               '--noproxy', '*'])

        received_file = os.path.join(self.file_path, file_name)

        if self.validate_file_md5_hash(received_file, file_hash):
            print '%s OK' % file_name
            self.file_fail_count = 0
            self.sendLine('FILE_OK')
            if not self.file_list.has_key(self.version):
                self.file_list[self.version] = {}
            self.file_list[self.version][self.file_temp['type']] = self.file_temp
        else:
            print '%s Failed %s times' % (file_name, self.file_fail_count)
            if self.file_fail_count != 5:
                self.file_fail_count += 1
                self.sendLine('RETRY')
            else:
                self.transport.loseConnection()

    def validate_file_md5_hash(self, file, original_hash):
        """ Returns true if file MD5 hash matches with the provided one, false otherwise. """

        if self.get_file_md5_hash(file) == original_hash:
            return True

        return False

    def read_bytes_from_file(self, file, chuck_size = 8100):
        # Read bytes from a file in chuck
        with open(file, 'rb') as file:
            while True:
                chuck = file.read(chuck_size)
                if chuck:
                    yield chuck
                else:
                    break;

    def get_file_md5_hash(self, file):
        md5_hash = hashlib.md5()
        print 'Hashing File: %s' % file
        for bytes in self.read_bytes_from_file(file):
            md5_hash.update(bytes)

        return md5_hash.hexdigest()

        ##### End of General Functions #####


##### Air Server Factory #####
class AirServerFactory(Factory):

    def __init__(self, path):
        self.devices = {}
        self.builders = {}
        self.file_path = path
        self.file_list = {} # File seperate by dict name 'Version'
                            # file_list['QBJM000.0.0001'] =
                            # {'IMG', 'GIT_LOG', 'TEST_ITEM_XML', 'TEST_RESULT_XML', 'TEST_RESULT_LOG'}

    def buildProtocol(self, addr):
        p = AirServer(self.builders ,self.devices, self.file_list, self.file_path)
        p.factory = self
        return p

##### Entry Function #####
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='A.I.R Server Daemon')
    parser.add_argument(
        '--ip',
        default = '127.0.0.1',
        help = 'Listen IP'
    )
    parser.add_argument(
        '--port',
        default = 8090,
        type = int,
        help = 'Listen Port'
    )
    parser.add_argument(
        '--path',
        default = os.getcwd(),
        help = 'Working Directory'
    )
    options = parser.parse_args()
    print '[AIR Server Started]'

    resource = TFile('%s' % options.path)
    fileserver = Site(resource)
    reactor.listenTCP(8091, fileserver)
    reactor.listenTCP(port=options.port, factory=AirServerFactory(options.path))
    reactor.run()