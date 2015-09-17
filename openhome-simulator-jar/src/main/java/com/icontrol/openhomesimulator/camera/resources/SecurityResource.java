package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.camera.CameraSimulator;
import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.*;
import com.icontrol.rest.framework.service.Endpoint;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.ohsimsolver.ResponseStatusFactory;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Resource("Security")
public class SecurityResource {

    protected static final Logger log = LoggerFactory.getLogger(SecurityResource.class);
    private static Map<String, Account> accountMap = new HashMap<String, Account>();

    static{
        accountMap = new HashMap<String, Account>() ;
        Account c = AaaAccountsResource.IdResource.ExampleResource.get();
        accountMap.put(c.getId().getValue(), c);
    }

    @Resource("AAA/accounts")
    public static class AaaAccountsResource {


        @Endpoint
        public UserList get() throws Exception {
            UserList list = new UserList();
            synchronized (accountMap){
                list.getAccount().addAll(accountMap.values());
            }
            return list;
        }

        @Endpoint
        public ResponseStatus put(UserList list) throws Exception{
            if (list==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            Iterator<Account> iter = list.getAccount().iterator();
            synchronized (accountMap){
                accountMap.clear();
                while (iter.hasNext()) {
                    Account a = iter.next();
                    accountMap.put(a.getId().getValue(), a);
                }
            }
            return ResponseStatusFactory.getResponseOK();

        }

        @Endpoint
        public ResponseStatus post(Account account) throws Exception{
            if (account==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            synchronized (accountMap){
                accountMap.put(account.getId().getValue(), account);
            }
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public ResponseStatus delete() throws Exception{
            synchronized (accountMap){
                accountMap = new HashMap<String, Account>() ;
                return ResponseStatusFactory.getResponseOK();
            }
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public UserList get() {
                Account c = IdResource.ExampleResource.get();
                UserList list = new UserList();
                list.getAccount().add(c);
                return list;
            }

        }

        @Resource("[UID]")
        public static class IdResource{
            @Endpoint
            public Account get(@PathVar("UID") String id) throws Exception {
                synchronized (accountMap){
                    Account a = accountMap.get(id);
                    if (a == null) {
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                    }
                    return a;
                }
            }

            @Endpoint
            public ResponseStatus put(@PathVar("UID") String id, Account account) throws Exception{
                synchronized (accountMap){
                    accountMap.put(id, account);
                }
                return ResponseStatusFactory.getResponseOK();
            }

            @Endpoint
            public ResponseStatus delete(@PathVar("UID") String id) throws Exception{
                synchronized (accountMap){
                    accountMap.remove(id);
                    return ResponseStatusFactory.getResponseOK();
                }
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                static public Account get(){
                    Account c = new Account();
                    c.setUserName(Wrappers.createStringCap("administrator"));
                    c.setPassword(Wrappers.createStringCap(""));
                    c.setAccessRights(AccessRightsType.ADMIN);
                    c.setId(Wrappers.createIdCap("0"));
                    return c;
                }
            }

        }

    }

    @Resource("Authorization")
    public static class AuthroizationResource {

        @Endpoint
        public AuthorizationInfo get() throws Exception{
            return AuthroizationResource.ExampleResource.get();
        }

        @Endpoint
        public ResponseStatus put(AuthorizationInfo info) throws Exception{
            CameraSimulator camera = CameraSimulatorFactory.getInstance().getCameraInstance();
            if (camera != null) {
                camera.setSiteID(info.getSiteID().getValue());
                camera.setSharedSecret(info.getSharedSecret().getValue());
                camera.setCredentialGwURL(info.getCredentialGWURL().getValue());
            }
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public AuthorizationInfo get() throws IOException {
                AuthorizationInfo info = new AuthorizationInfo();
                CameraSimulator camera = CameraSimulatorFactory.getInstance().getCameraInstance();
                if (camera != null) {
                    info.setSiteID(Wrappers.createStringCap(camera.getSiteID()));
                    info.setSharedSecret(Wrappers.createStringCap(camera.getSharedSecret()));
                    info.setPendingKey(Wrappers.createStringCap(camera.getSharedSecret()));
                    info.setCredentialGWURL(Wrappers.createStringCap(camera.getCredentialGwURL()));
                } else {
                    info.setSiteID(Wrappers.createStringCap("siteid"));
                    info.setSharedSecret(Wrappers.createStringCap("shared secret"));
                    info.setPendingKey(Wrappers.createStringCap("pending key"));
                    info.setCredentialGWURL(Wrappers.createStringCap("credential gateway url"));
                }
                return info;
            }
        }
    }

    @Resource("updateSSLCertificate/client")
    public static class UpdateSSLCertClientResource {

        @Endpoint
        public String get() throws Exception{
            String pem = "<clientCertList><clientCert >-----BEGIN CERTIFICATE-----\n" +
                    "MIIBvjCCAWigAwIBAgIETsaZtTANBgkqhkiG9w0BAQUFADBmMQswCQYDVQQGEwJVUzELMAkGA1UE\n" +
                    "CBMCQ0ExFTATBgNVBAcTDFJlZHdvb2QgQ2l0eTERMA8GA1UEChMIaUNvbnRyb2wxETAPBgNVBAsT\n" +
                    "CE9wZW5Ib21lMQ0wCwYDVQQDEwRYTVBQMB4XDTExMTExODE3NDUyNVoXDTIxMTExNTE3NDUyNVow\n" +
                    "ZjELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRUwEwYDVQQHEwxSZWR3b29kIENpdHkxETAPBgNV\n" +
                    "BAoTCGlDb250cm9sMREwDwYDVQQLEwhPcGVuSG9tZTENMAsGA1UEAxMEWE1QUDBcMA0GCSqGSIb3\n" +
                    "DQEBAQUAA0sAMEgCQQCLeh4kzUMxWEoQI8C4QCaZ8ykNH+2ulyLKpwZmsn1JWxwDg8vOFey2c9pn\n" +
                    "sK/3phS/FQjTCdc3LvFIA4mNIBK/AgMBAAEwDQYJKoZIhvcNAQEFBQADQQAOEGb+450YwGl5AIIq\n" +
                    "NUPYDNwOgTE70g4BYX6mUsQ3KKRSAPyQireeA/XtFBkPsVv+TzNKR0AhkulGcz/fugIp\n" +
                    "-----END CERTIFICATE-----</clientCert></clientCertList>";
            return pem;
        }

        @Endpoint
        public ResponseStatus post() throws Exception{
            System.out.println("POST updatesslcertificate/client - Did nothing.");
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public UpdateSSLCertCommand get() {
                UpdateSSLCertCommand cmd = new UpdateSSLCertCommand();
                UpdateSSLCertCommand.ClientCert clientCert = new UpdateSSLCertCommand.ClientCert();
                clientCert.setUrl(Wrappers.createStringCap("url of cert source"));
                cmd.setClientCert(clientCert);
                return cmd;
            }
        }
    }
}
