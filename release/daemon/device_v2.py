#
# (C) Copyright 2011-2012 Compal Electronics, Inc.
#
# This software is the property of Compal Electronics, Inc.
# You have to accept the terms in the license file before use.
#
__author__ = "Scott Hsieh"
__copyright__ = "Copyright 2011-2012 Compal Electronics, Inc."

from twisted.protocols.basic import LineReceiver
from twisted.internet import reactor, threads
from twisted.internet.protocol import ClientFactory, Protocol
# Http Server
from twisted.web.server import Site
from twisted.web.static import File

import socket, os, argparse, hashlib, subprocess, shutil

class Device(LineReceiver):

    def __init__(self, server_ip, server_port, project, name):
        self.server_ip = server_ip
        self.server_port = server_port
        self.project = project
        self.name = name
        self.file_path = os.getcwd()
        self.state = 'REG_DEVICE'
        # Prepare for File Transfer
        self.file_temp = None # File Receive Queue
        self.current_file = None # File Send Queue
        self.file_list = {}
        self.file_fail_count = 0
        self.test_status = None

        self.DEBUG = False
        self.KEEP_TEST = False

    def connectionMade(self):
        print '[Device Ready]'

    def connectionLost(self, reason):
        self.file_list = {}
        print '[Server Lost Connection]'
        reactor.stop()

    def lineReceived(self, line):
        print 'Line Received: %s' % line
        print 'State: %s' % self.state
        if self.state == 'REG_DEVICE':
            self.transport.write('DEVICE|%s|%s\r\n' % (self.name, self.project))
            if self.factory.test_only: # Just do test without builder trigger
                self.version = self.factory.test_only
                self.sendLine('SET_VERSION')
                self.state = 'CHANGE_VERSION'
            else:
                self.state = 'WAIT_COMMAND'
        elif self.state == 'WAIT_COMMAND':
            self.handle_WAIT_COMMAND(line)
        elif self.state == 'GET_FILE_INFO':
            self.handle_GET_FILE_INFO(line)
        elif self.state == 'GET_VERSION_INFO':
            self.handle_GET_VERSION_INFO(line)
        elif self.state == 'CHANGE_VERSION':
            self.handle_CHANGE_VERSION(line)
        elif self.state == 'SEND_FILE_INFO':
            self.handle_SEND_FILE_INFO(line)
        elif self.state == 'SEND_FILE':
            self.handle_SEND_FILE(line)
        elif self.state == 'WAIT_SAVE':
            self.handle_WAIT_SAVE(line)
        elif self.state == 'WAIT_SEND_MAIL':
            self.handle_WAIT_SEND_MAIL(line)

    def handle_WAIT_COMMAND(self, line):
        if line == 'GET_VERSION_INFO':
            self.state = 'GET_VERSION_INFO'
        elif line == 'GET_FILE_INFO':
            self.state = 'GET_FILE_INFO'
            self.sendLine('SEND_FILE_INFO')
            self.sendLine('READY')
        elif line == 'GET_FILE':
            if not self.factory.local_file:
                self.download_file()
            else:
                if not os.path.exists(self.file_temp['path']):
                    self.download_file()
                else:
                    self.sendLine('FILE_OK')
                    self.file_list[self.file_temp['type']] = self.file_temp

        elif line == 'START_TEST':
            self.auto_test()

    ##### Get Version Functions #####
    def handle_GET_VERSION_INFO(self, line):
        if line.startswith('VERSION'):
            data = line.split(':')
            # 0:VERSION Command, 1: VERSION
            self.version = data[1]
            print 'Testing %s' % self.version
            # Tell my server thread to change version
            self.sendLine('SET_VERSION')
            self.state = 'CHANGE_VERSION'
        else:
            print 'State: %s, Command: %s' % (self.state, line)

    def handle_CHANGE_VERSION(self, line):
        if line == 'READY':
            self.sendLine('VERSION:%s' % self.version)
            self.state = 'WAIT_COMMAND'

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
                self.current_file = self.file_list.pop()

            print 'Sending File Info %s' % self.current_file['name']
            self.sendLine('HASH:%s:%s:%s' % (self.current_file['type'], self.current_file['name'], self.current_file['hash']))
            self.state = 'SEND_FILE'

    def handle_SEND_FILE(self, line):
        if line == 'FILE_OK':
            print '%s Send Success' % self.current_file['name']
            if self.file_list:
                self.current_file = self.file_list.pop()
                self.state = 'SEND_FILE_INFO'
                self.sendLine('GET_FILE_INFO')
            else:
                print 'All file send, waiting for result save'
                self.sendLine('SAVE_RESULT')
                self.state = 'WAIT_SAVE'
        else:
            self.sendLine('GET_FILE')
            print 'Please download file from http://%s:%s/' % (self.transport.getHost().host, '8093')

    def handle_WAIT_SAVE(self, line):
        if line == 'RESULT_OK':
            print '[Result OK]'
            self.file_temp = None # File Receive Queue
            self.current_file = None # File Send Queue
            self.file_list = {}
            if self.test_status:
                self.sendLine('SEND_SUCCESS_MAIL')
            else:
                self.sendLine('SEND_TIMEOUT_MAIL')
            self.state = 'WAIT_SEND_MAIL'

    def handle_WAIT_SEND_MAIL(self, line):
        if line == 'MAIL_OK':
            print '[Mail Send]'
            if self.KEEP_TEST:
                print '[Wait for next text]'
                print '[Please set your device to Recover Mode]'
                self.state = 'WAIT_COMMAND'
            else:
                self.transport.loseConnection()

    ##### End of Send File Functions #####

    ##### General Functions #####

    def auto_test(self):
        print 'Start Testing'
        if self.DEBUG:
            subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/all.sh', '--noproxy', '*'])
            subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/TestItemResult.xml', '--noproxy', '*'])
            subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/AirLog.zip', '--noproxy', '*'])
            #subprocess.check_call(['bash', 'all.sh'])
            if self.timeout_command(['bash', 'all.sh'], self.factory.timeout):
                self.test_status = True
            else:
                self.test_status = False

            shutil.copy(self.file_list['CONFIG_XML']['path'], 'TestItemConfig.xml')
            print self.file_list['IMG']['path']
        else:
            if not self.factory.local_file:
                subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/air.sh', '--noproxy', '*'])
                subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/AirGetResult.sh', '--noproxy', '*'])
                subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/air.zip', '--noproxy', '*'])
                subprocess.check_call(['chmod', 'a+x', 'air.sh'])
                subprocess.check_call(['chmod', 'a+x', 'AirGetResult.sh'])
                shutil.copy(self.file_list['CONFIG_XML']['path'], 'TestItemConfig.xml')
            else:
                if not os.path.isfile('air.sh'):
                    subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/air.sh', '--noproxy', '*'])
                    subprocess.check_call(['chmod', 'a+x', 'air.sh'])
                elif not os.path.isfile('AirGetResult.sh'):
                    subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/AirGetResult.sh', '--noproxy', '*'])
                    subprocess.check_call(['chmod', 'a+x', 'AirGetResult.sh'])
                elif not os.path.isfile('air.zip'):
                    subprocess.check_call(['curl', '-OL', 'http://10.109.39.9/tools/autobuild/air.zip', '--noproxy', '*'])
                elif not os.path.isfile('TestItemConfig.xml'):
                    shutil.copy(self.file_list['CONFIG_XML']['path'], 'TestItemConfig.xml')
            #subprocess.check_call(['bash','air.sh', self.file_list['IMG']['path'], 'air.zip', 'TestItemConfig.xml'])
            if self.timeout_command(['bash','air.sh', self.file_list['IMG']['path'], 'air.zip', 'TestItemConfig.xml'], self.factory.timeout):
                self.test_status = True
            else:
                self.test_status = False

            subprocess.check_call(['bash','AirGetResult.sh'])

        # Clean file list and prepare for upload file
        self.file_list = []
        self.file_list.append(dict(
            type='RESULT_XML',
            name='TestItemResult.xml',
            path=os.path.join(self.file_path, 'TestItemResult.xml'),
            hash=self.get_file_md5_hash(os.path.join(self.file_path, 'TestItemResult.xml')))
        )
        self.file_list.append(dict(
            type='RESULT_LOG',
            name='AirLog.zip',
            path=os.path.join(self.file_path, 'AirLog.zip'),
            hash=self.get_file_md5_hash(os.path.join(self.file_path, 'AirLog.zip')))
        )
        self.sendLine('GET_FILE_INFO')
        self.state = 'SEND_FILE_INFO'

    def download_file(self):
        file_name = self.file_temp['name']
        file_hash = self.file_temp['hash']
        print 'Download file %s' % file_name

        subprocess.check_call(['curl', '-OL',
                               'http://%s:%s/%s' % (self.transport.getPeer().host, 8091, file_name),
                               '--noproxy', '*'])

        received_file = os.path.join(self.file_path, file_name)

        if self.validate_file_md5_hash(received_file, file_hash):
            print '%s OK' % file_name
            self.file_fail_count = 0
            self.sendLine('FILE_OK')
            self.file_list[self.file_temp['type']] = self.file_temp
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

    def timeout_command(self, command, timeout):
        """call shell-command and either return its output or kill it
        if it doesn't normally exit within timeout seconds and return None"""
        import subprocess, datetime, os, time, signal
        start = datetime.datetime.now()
        process = subprocess.Popen(command)
        while process.poll() is None:
            time.sleep(1)
            now = datetime.datetime.now()
            if (now - start).seconds> timeout:
                os.kill(process.pid, signal.SIGKILL)
                os.waitpid(-1, os.WNOHANG)
                return False
        return True

    ##### End of General Functions #####

class DeviceFactory(ClientFactory):

    def __init__(self, server_ip, port, project, name, timeout, testonly, local):
        self.server_ip = server_ip
        self.server_port = port
        self.project = project
        self.name = name
        self.timeout = timeout
        self.file_path = os.getcwd()
        self.test_only = testonly
        self.local_file = local


    def buildProtocol(self, addr):
        print '[Connected]'
        p = Device(self.server_ip, self.server_port, self.project, self.name)
        p.factory = self
        return p

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='A.I.R Builder Daemon')
    parser.add_argument(
        '--ip',
        default = '127.0.0.1',
        help = 'server ip')
    parser.add_argument(
        '--port',
        dest = 'port',
        default = 8090,
        type = int,
        help = 'Server Port'
    )
    parser.add_argument(
        'project',
        help = 'Project Name'
    )
    parser.add_argument(
        '--name',
        default = socket.gethostname(),
        help = 'Client Name'
    )
    parser.add_argument(
        '--timeout',
        default = 3000,
        type = int,
        help = 'Time out check(sec)'
    )
    parser.add_argument(
        '--testonly',
        default = None,
        help = 'Do test only(version)'
    )
    parser.add_argument(
        '--local',
        default = False,
        action = 'store_true',
        help = 'Use local file(image and config)'
    )

    options = parser.parse_args()
    print '[Client Started]'

    resource = File('%s' % os.getcwd())
    fileserver = Site(resource)
    reactor.listenTCP(8093, fileserver)

    reactor.connectTCP(options.ip, options.port, DeviceFactory(
        options.ip,
        options.port,
        options.project,
        options.name,
        options.timeout,
        options.testonly,
        options.local))
    reactor.run()
