from argparse import Namespace
import pycurl
import unittest
from agate.core import AgateClient


class AgateClientTestSSLConnection(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        setattr(cls, 'PORT', '8081')
        setattr(cls, 'SERVER', 'http://localhost')
        setattr(cls, 'SSL_PORT', '8444')
        setattr(cls, 'SSL_SERVER', 'https://localhost')
        # Make sure to place your own certificate files
        setattr(cls, 'SSL_CERTIFICATE', '../../resources/certificates/publickey.pem')
        setattr(cls, 'SSL_KEY', '../../resources/certificates/privatekey.pem')

    def test_sendRestBadServer(self):
        client = AgateClient.buildWithAuthentication(server='http://deadbeef:8081', user='administrator',
                                                    password='password')

        self.assertRaises(Exception, self.__sendSimpleRequest, client.new_request())

    def test_sendRestBadCredentials(self):
        client = AgateClient.buildWithAuthentication(server="%s:%s" % (self.SERVER, self.PORT), user='admin',
                                                    password='password')

        self.assertRaises(Exception, self.__sendSimpleRequest, client.new_request())

    def test_sendRest(self):
        try:
            client = AgateClient.buildWithAuthentication(server="%s:%s" % (self.SERVER, self.PORT), user='administrator',
                                                        password='password')
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_sendSecuredRest(self):
        try:
            client = AgateClient.buildWithCertificate(server="%s:%s" % (self.SSL_SERVER, self.SSL_PORT),
                                                     cert=self.SSL_CERTIFICATE,
                                                     key=self.SSL_KEY)
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_validAuthLoginInfo(self):
        try:
            args = Namespace(agate="%s:%s" % (self.SERVER, self.PORT), user='administrator', password='password')
            client = AgateClient.build(loginInfo=AgateClient.LoginInfo.parse(args))
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_validSslLoginInfo(self):
        try:
            args = Namespace(agate="%s:%s" % (self.SSL_SERVER, self.SSL_PORT), ssl_cert=self.SSL_CERTIFICATE,
                             ssl_key=self.SSL_KEY)
            client = AgateClient.build(loginInfo=AgateClient.LoginInfo.parse(args))
            self.__sendSimpleRequest(client.new_request())
        except Exception, e:
            self.fail(e)
        except pycurl.error, error:
            self.fail(error)

    def test_invalidServerInfo(self):
        args = Namespace(opl="%s:%s" % (self.SERVER, self.PORT), user='administrator', password='password')
        self.assertRaises(Exception, AgateClient.LoginInfo.parse, args);

    def test_invalidLoginInfo(self):
        args = Namespace(agate="%s:%s" % (self.SERVER, self.PORT), usr='administrator', password='password')
        self.assertRaises(Exception, AgateClient.LoginInfo.parse, args);

    def __sendSimpleRequest(self, request):
        request.fail_on_error()
        request.accept_json()
        # uncomment for debugging
        # request.verbose()

        # send request
        request.method('GET').resource('/studies')
        response = request.send()

        # format response
        res = response.content

        # output to stdout
        print res

