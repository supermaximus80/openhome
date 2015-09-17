package com.icontrol.openhomesimulator.gateway.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import com.icontrol.openhomesimulator.util.ActivationKeyGen;
import com.icontrol.openhomesimulator.util.OpenHomeProperties;
import com.icontrol.rest.framework.service.Resource;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class BootStrapResource {
    protected static final Logger log = LoggerFactory.getLogger(BootStrapResource.class);

    /*
        static resource data
     */
    private static Map<String, String> serialToSiteIDmap = new ConcurrentHashMap<String, String>();
    private static Map<String, SecretKeyInfo> siteIDtoSecretInfomap = new ConcurrentHashMap<String, SecretKeyInfo>();

    private static String siteIDPrefix = "00f066";
    private static long currentSiteIDindex = 1;

    private static int SERIAL_NUMBER_LENGTH = 12;
    private static int SECRET_KEY_LENGTH = 8;
    private static long PENDING_KEY_EXPIRATION = 10*60*1000;    // 10 min

    private static Random random = new Random();


    static {
        try {
            // initialize serialNoToSiteIDmap
            String sn = OpenHomeProperties.getProperty("cameraSimulator.serialNo");
            String pwd = OpenHomeProperties.getProperty("cameraSimulator.sharedSecret");
            String siteID = OpenHomeProperties.getProperty("cameraSimulator.siteId");
            if (sn != null && pwd != null) {
                if (siteID==null || siteID.length()==0)
                    siteID = siteIDPrefix + String.format("%06d",currentSiteIDindex++);
//                  siteID = "120116yc345507";
                serialToSiteIDmap.put(sn, siteID) ;
                SecretKeyInfo keyInfo = new SecretKeyInfo();
                keyInfo.setKey(pwd);
                siteIDtoSecretInfomap.put(siteID,  keyInfo);
            }
        } catch (Exception ex) {
            log.error("init caught "+ex);
        }
    }

    @Resource("registry/[serialNo]")
    public static class RegistryResource {
        private final static String pathName = "registry/";
        @Endpoint
        public RegistryEntry get(HttpServletRequest req,  @PathVar("serialNo") String serialNo) throws Exception {
            if (serialNo == null || serialNo.trim().length() < SERIAL_NUMBER_LENGTH  ) {
                log.error("Invalid serialNo="+serialNo);
                throw new IOException("Invalid serialNo="+serialNo) ;
            }
            // lookup siteId from serial #
            serialNo = serialNo.trim();
            String siteID = serialToSiteIDmap.get(serialNo) ;
            if (siteID == null) {   // generate a siteID
                siteID = siteIDPrefix + String.format("%06d",currentSiteIDindex++);
                log.debug("Creating siteId: " + siteID);
                serialToSiteIDmap.put(serialNo, siteID);
                log.debug("Registry just stored serialNo: "+serialNo+" siteID: "+siteID);
            }

            RegistryEntry re = new RegistryEntry();
            re.setSiteId(siteID);
            // gatewayURL
            String protocol = "http";
            if (req.getLocalPort() == 8443) {
                protocol = "https";
            }
            String gwUrl = protocol+"://"+req.getLocalName()+":"+req.getLocalPort()+req.getRequestURI();
            //String gwUrl = protocol+"://"+req.getLocalAddr()+":"+req.getLocalPort()+req.getRequestURI();
            int pos = gwUrl.indexOf(pathName);
            if (pos == -1) {
                log.error("Invalid request url="+gwUrl);
                throw new Exception("Invalid request url="+gwUrl) ;
            }
            gwUrl = gwUrl.substring(0,pos);
            if (gwUrl.endsWith("/"))
                gwUrl = gwUrl.substring(0, gwUrl.length()-1);

            log.debug("Retrieved registry entry - gateway URL: " + gwUrl);
            re.setGatewayUrl(gwUrl);
            return re;
        }
    }

    @Resource("gatewayservice/[siteID]/pendingdevicekey")
    public static class PendingDeviceKeyResource {
        @Endpoint
        public PendingPaidKey post(HttpServletRequest req, @PathVar("siteID") String siteID, String body) throws IOException, javax.xml.datatype.DatatypeConfigurationException {
            // parse input string in the format serial=<Serial Number>&activationkey=<ActivationKey>
            if (body == null) {
                log.error("Invalid post body: "+body);
                throw new IOException("Invalid post body: "+body);
            }

            String serialNo = getField(body, "serial=") ;
            String activationKey = getField(body, "activationkey=");

            // check if siteID and serial No matches
            String sidMatch = serialToSiteIDmap.get(serialNo);
            if (!siteID.equalsIgnoreCase(sidMatch) ) {
                log.error("SerialNo:"+serialNo+" and siteID:"+siteID+" does NOT match stored siteID:"+sidMatch);
                throw new IOException("SerialNo:"+serialNo+" and siteID:"+siteID+" does NOT match stored siteID:"+sidMatch) ;
            }

            // check if acticationKey is valid
            if (OpenHomeProperties.getProperty("activationKey.enabled", false)) {
                log.debug("ActivationKey Check enabled");
                String vendorKey = OpenHomeProperties.getProperty("activationKey.vendorKey", "");
                if (vendorKey.length()==0) {
                    log.error("ActivationKey invalid vendor key");
                    throw new IOException("ActivationKey invalid vendor key");
                }

                String expectedKey = ActivationKeyGen.generateActivationKey(serialNo, vendorKey);
                if (!expectedKey.equalsIgnoreCase(activationKey)) {
                    log.error("ActivationKey Checked - failed validation. key:"+activationKey+" expectedKey:"+expectedKey);
                    throw new IOException("ActivationKey Checked - failed validation. key:"+activationKey+" expectedKey:"+expectedKey);
                }
            }

            // determine if successful XMPP connection was completed within expiration period
            boolean isPendingKey = true;    // TODO

            // create a pending key
            SecretKeyInfo keyInfo = new SecretKeyInfo();

            // Set static secret key for specified camera serial#
            // initially targetted for testing digest challenge but now for forcing the same sharedSecret in general
            if (serialNo.equals(OpenHomeProperties.getProperty("cameraSimulator.serialNo"))) {
                keyInfo.setKey(OpenHomeProperties.getProperty("cameraSimulator.sharedSecret"));
            }

            // create response
            PendingPaidKey pk = new PendingPaidKey();
            if (isPendingKey) {
                pk.setMethod("server");
                pk.setKey(keyInfo.getKey());
            } else {
                pk.setMethod("retry");
            }

            pk.setPartner("icontrol");
            pk.setTs(keyInfo.getStartTs());
            pk.setExpires(keyInfo.getExpires());

            // store pending key
            if (isPendingKey) {
                siteIDtoSecretInfomap.put(siteID, keyInfo);
            }

            log.debug("Retrieved credentials for - serialNo: "+serialNo+" siteID: "+siteID+" secretKey: "+keyInfo.getKey());
            return pk;
        }

        private String getField(String str, String keyword) throws IOException {
            str = str.trim().toLowerCase();
            int pos = str.indexOf(keyword);
            if (pos == -1) {
                log.error("Can't find keyword:"+keyword);
                throw new IOException("Can't find keyword:"+keyword);
            }

            // strip out keyword from beg of str
            str = str.substring(pos+keyword.length());
            // find end
            int pos2 = str.indexOf("&");
            if (pos2 == -1)
                pos2 = str.length();
            return str.substring(0, pos2);
        }
    }

    @Resource("gatewayservice/[siteID]/connectinfo")
    public static class ConnectInfoResource {
        @Endpoint
        public ConnectInfo get(HttpServletRequest req, @PathVar("siteID") String siteID) throws IOException, javax.xml.datatype.DatatypeConfigurationException {
            // check if siteID is already registered
            SecretKeyInfo key = siteIDtoSecretInfomap.get(siteID);
            if (key == null) {
                log.error("ConnectInfo Unknown siteID: "+siteID);
                throw new IOException("ConnectInfo Unknown siteID: "+siteID);
            }

            ConnectInfo info = new ConnectInfo();
            // session server info
            ConnectInfo.Session session = (new ConnectInfo.Session());
            String server = req.getServerName();
            session.setHost(server);
            session.setPort(new BigInteger("443"));
            info.setSession(session);

            // session server info
            ConnectInfo.Xmpp xmpp = (new ConnectInfo.Xmpp());
            String host =  GatewaySimulatorFactory.getInstance().getXmppServerDomainName(req);
            BigInteger port = new BigInteger(Integer.toString(GatewaySimulatorFactory.getInstance().getXmppServerPort()));
            xmpp.setHost(host);
            xmpp.setPort(port);
            info.setXmpp(xmpp);

            if (GatewaySimulatorFactory.getInstance().isbEnableXmppMode())
                log.debug("Retrieved connect info to reach xmpp Server: " + host + ":" + port);
            return info;
        }
    }

    /*
        SecretKeyInfo
     */
    private static class SecretKeyInfo {

        private String key;
        private boolean pending;
        private long startTs;
        private long expires;

        SecretKeyInfo() {
            key = Long.toHexString(random.nextLong());
            if (key.length() > SECRET_KEY_LENGTH)
                key = key.substring(0, SECRET_KEY_LENGTH);
            pending = true;
            startTs = System.currentTimeMillis();
            expires = startTs + PENDING_KEY_EXPIRATION;

        }

        // Only for simulator
        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public boolean isPending() {
            return pending;
        }

        public long getExpires() {
            return expires;
        }

        public void setPending(boolean pending) {
            this.pending = pending;
        }

        public long getStartTs() {
            return startTs;
        }

    }

    /*
        getSharedSecret
     */
    public static String getSharedSecretViaSerialNo(String serialNo) throws UserNotFoundException {
        // look up siteID
        String siteID = serialToSiteIDmap.get(serialNo);
        if (siteID == null) {
            String error = "No record exists for serialNo: " + serialNo;
            log.error(error);
            throw new UserNotFoundException(error);
        }

        SecretKeyInfo secret = siteIDtoSecretInfomap.get(siteID);
        if (secret == null) {
            String error = "No secret key exists for serialNo: " + serialNo;
            log.error(error);
            throw new UserNotFoundException(error);
        }

        return secret.getKey();
    }

    public static String getSharedSecretViaSerialNo(String serialNo, String siteID) throws UserNotFoundException {
        // look up siteID
        String siteIDstored = serialToSiteIDmap.get(serialNo);
        if (siteIDstored == null || !siteID.equalsIgnoreCase(siteIDstored)) {
            String error = "No record exists for serialNo: " + serialNo + " for siteId: " + siteID;
            log.error(error);
            throw new UserNotFoundException(error);
        }

        SecretKeyInfo secret = siteIDtoSecretInfomap.get(siteID);
        if (secret == null) {
            String error = "No secret key exists for serialNo: " + serialNo + " for siteId: " + siteID;
            log.error(error);
            throw new UserNotFoundException(error);
        }

        return secret.getKey();
    }
}
