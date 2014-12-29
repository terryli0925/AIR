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

import socket, os, argparse, hashlib

class Builder(LineReceiver):

    def __init__(self, server_ip, server_port, project, version):
        self.server_ip = server_ip
        self.server_port = server_port
        self.project = project
        self.version = version
        self.file_path = os.getcwd()
        self.state = 'REG_BUILDER'
        self.current_file = None # File Transfer Queue

    def connectionMade(self):
        # Prepare for File Transfer
        self.file_list = []
        img_path = os.path.join(self.file_path, self.factory.file_img)
        log_path = os.path.join(self.file_path, self.factory.file_log)
        self.file_list.append({'type': 'IMG',
                               'name': self.factory.file_img,
                               'path': img_path,
                               'hash': self.get_file_md5_hash(img_path)})
        self.file_list.append({'type': 'LOG',
                               'name': self.factory.file_log,
                               'path': log_path,
                               'hash': self.get_file_md5_hash(log_path)})
        print '[FileTransfer Ready]'

    def connectionLost(self, reason):
        self.file_list = []

        print '[FileTransfer Lost Connection]'
        reactor.stop()

    def lineReceived(self, line):
        print 'Line Received: %s' % line

        if self.state == 'REG_BUILDER':
            self.transport.write('BUILDER|%s|%s|%s\r\n' % (socket.gethostname(), self.project, self.version))
            self.sendLine('GET_FILE_INFO')
            self.state = 'SEND_FILE_INFO'
        elif self.state == 'SEND_FILE_INFO':
            self.handle_SEND_FILE_INFO(line)
        elif self.state == 'SEND_FILE':
            self.handle_SEND_FILE(line)
        elif self.state == 'WAIT_SAVE':
            self.handle_WAIT_SAVE(line)

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
                print 'All file send, waiting for version save'
                self.sendLine('SAVE_VERSION')
                self.state = 'WAIT_SAVE'
        else:
            self.sendLine('GET_FILE')
            print 'Please download file from http://%s:%s/' % (self.transport.getHost().host, '8092')

    def handle_WAIT_SAVE(self, line):
        if line == 'VERSION_OK':
            print '[Version Saved]'
            self.transport.loseConnection()

    ##### General Functions #####

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

class BuilderFactory(ClientFactory):

    def __init__(self, server_ip, port, project, version, img, log):
        self.server_ip = server_ip
        self.server_port = port
        self.project = project
        self.version = version
        self.file_img = img
        self.file_log = log
        self.file_path = os.getcwd()

    def buildProtocol(self, addr):
        print '[Connected]'
        p = Builder(self.server_ip, self.server_port, self.project, self.version)
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
        help = 'Project')
    parser.add_argument(
        'version',
        help = 'Version')
    parser.add_argument(
        'img',
        help = 'Image file')
    parser.add_argument(
        'log',
        help = 'Changelog file')

    options = parser.parse_args()
    print '[Client Started]'

    resource = File('%s' % os.getcwd())
    fileserver = Site(resource)
    reactor.listenTCP(8092, fileserver)

    reactor.connectTCP(options.ip, options.port, BuilderFactory(
        options.ip,
        options.port,
        options.project,
        options.version,
        options.img,
        options.log))
    reactor.run()