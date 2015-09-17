package com.icontrol.android.openhomesimulator.camera.resources;

import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.RestfulResponse;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Resource("system/network/interfaces")
public class SystemNetworkInterfacesResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemNetworkInterfacesResource.class);
    private static Map<String, NetworkInterface> interfaceMap = new ConcurrentHashMap<String, NetworkInterface>();
    private static String WIRELESS_INTERFACE_ID = "1";

    static {
        NetworkInterface networkInterface = ExampleResource.get();
        interfaceMap.put(networkInterface.getId().getValue(),networkInterface);
    }

    @Endpoint
    public NetworkInterfaceList get() throws Exception {
        NetworkInterfaceList interfaceList = new NetworkInterfaceList();
        interfaceList.getNetworkInterface().addAll(interfaceMap.values());
        return interfaceList;
    }

    public static class ExampleResource{

        static public NetworkInterface get(){
            NetworkInterface net = IdResource.ExampleResource.get();
            net.setEnabled(Wrappers.createBooleanCap(false));
            return net;
        }
    }

    @Resource("[id]")
    public static class IdResource{
        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public NetworkInterface get(){
                NetworkInterface net = new NetworkInterface();
                // ipAddress
                IPAddress ipAddr = new IPAddress();
                // ipVer
                IPAddress.IpVersion ipVer = new IPAddress.IpVersion();
                ipVer.setValue("v4");
                ipAddr.setIpVersion(ipVer);
                // addressingType
                IPAddress.AddressingType type = new IPAddress.AddressingType();
                type.setValue("dynamic");
                ipAddr.setAddressingType(type);
                net.setIPAddress(ipAddr);
                net.setId(Wrappers.createIdCap("0"));
                return net;
            }
        }

        @Endpoint
        public NetworkInterface get(@PathVar("id") String id) throws Exception {
            NetworkInterface net = interfaceMap.get(id);
            if (net == null) {
                throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
            }
            return net;
        }

        @Endpoint
        public ResponseStatus put(@PathVar("id") String id, NetworkInterface net) throws Exception{
            //log.debug("Received "+pathPrefix+id);
            if (net==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            if (!id.equals("0") && !id.equals("1")) {
                log.error("Interface id must be either 0 or 1");
                return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT) ;
            }
            interfaceMap.put(id, net);
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("wireless")
        public static class WirelessResource {
            private static final String pathPrefix = "system/network/interfaces/";
            private static Wireless wirelessInterface = WirelessResource.ExampleResource.get();

            @Endpoint
            public Wireless get(@PathVar("id") String id) throws Exception {
                if (!WIRELESS_INTERFACE_ID.equals(id)) {
                    log.error(pathPrefix+" Invalid interface id for wireless. id="+id);
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return wirelessInterface;
            }

            @Endpoint
            public ResponseStatus put(@PathVar("id") String id, Wireless wireless) throws Exception{
                //log.debug("Received "+pathPrefix+id);
                if (wireless==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                if (!id.equals("1")) {
                    log.error("Interface id must be 1");
                    return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_XML_CONTENT) ;
                }
                wirelessInterface = wireless;
                return ResponseStatusFactory.getResponseOK();
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                static public Wireless get(){
                    Wireless w = new Wireless();
                    // enabled
                    w.setEnabled(Wrappers.createBooleanCap(true));
                    // ssid
                    w.setSsid(Wrappers.createStringCap("EmulatorWifiName"));
                    // wireless security
                    Wireless.WirelessSecurity security = new Wireless.WirelessSecurity();
                    // mode
                    Wireless.WirelessSecurity.SecurityMode mode = new Wireless.WirelessSecurity.SecurityMode();
                    mode.setValue("WPA2-personal");
                    security.setSecurityMode(mode);
                    // wpa
                    Wireless.WirelessSecurity.WPA wpa =  new Wireless.WirelessSecurity.WPA();
                    Wireless.WirelessSecurity.WPA.AlgorithmType algoType = new Wireless.WirelessSecurity.WPA.AlgorithmType();
                    algoType.setValue("TKIP/AES");
                    wpa.setAlgorithmType(algoType);
                    wpa.setSharedKey(Wrappers.createStringCap("SharedSecretHere"));
                    security.setWPA(wpa);
                    // refresh
                    w.setStatusRefreshInterval(Wrappers.createIntegerCap(15));

                    return w;
                }
            }

            @Resource("status")
            public static class StatusResource{
                @Endpoint
                public WirelessNetworkStatus get(@PathVar("id") String id) throws Exception {
                    if (!WIRELESS_INTERFACE_ID.equals(id)) {
                        log.error(pathPrefix+" Invalid interface id for wireless. id="+id);
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND,"");
                    }
                    WirelessNetworkStatus s = create();
                    return s;
                }

                static public WirelessNetworkStatus create() throws javax.xml.datatype.DatatypeConfigurationException {
                    WirelessNetworkStatus w = new WirelessNetworkStatus();
                    // enabled
                    w.setEnabled(Wrappers.createBooleanCap(true));
                    // channel No
                    WirelessNetworkStatus.ChannelNo channelNo = new WirelessNetworkStatus.ChannelNo();
                    channelNo.setValue("4");
                    w.setChannelNo(channelNo);
                    // ssid
                    w.setSsid(Wrappers.createStringCap("EmulatorWifiName"));
                    // bssid
                    w.setBssid(Wrappers.createStringCap("0:23:69:58:d1:85"));
                    // rssidB
                    w.setRssidB(Wrappers.createIntegerCap(-31));
                    // signalStrength
                    w.setSignalStrength(Wrappers.createPercentageCap(80));
                    // noisedB
                    w.setNoiseIndB(Wrappers.createIntegerCap(-87));
                    //numOfAP
                    w.setNumOfAPs(Wrappers.createIntegerCap(1));
                    // AvailableAPList
                    WirelessNetworkStatus.AvailableAPList apList = new WirelessNetworkStatus.AvailableAPList();
                    // ap
                    WirelessNetworkStatus.AvailableAPList.AvailableAccessPoint ap = new WirelessNetworkStatus.AvailableAPList.AvailableAccessPoint();
                    ap.setSsid(Wrappers.createStringCap("EmulatorWifiName"));
                    ap.setBssid(Wrappers.createStringCap("0:23:69:58:d1:85"));
                    ap.setRssidB(Wrappers.createIntegerCap(-31));
                    // ap security mode
                    WirelessNetworkStatus.AvailableAPList.AvailableAccessPoint.SecurityMode mode = new WirelessNetworkStatus.AvailableAPList.AvailableAccessPoint.SecurityMode();
                    mode.setValue("WPA2-personal");
                    ap.setSecurityMode(mode);

                    apList.getAvailableAccessPoint().add(ap) ;
                    w.setAvailableAPList(apList);

                    return w;
                }
            }
        }

        @Resource("ipaddress")
        public static class IpAddressResource{
            @Endpoint
            public IPAddress get(@PathVar("id") String id) throws Exception {
                NetworkInterface net = interfaceMap.get(id);
                if (net == null) {
                    log.error("Invalid interface id: " + id);
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND,"");
                }
                return interfaceMap.get(id).getIPAddress();
            }

            @Endpoint
            public ResponseStatus put(@PathVar("id") String id, IPAddress ipAddress) throws javax.xml.datatype.DatatypeConfigurationException {
                //log.debug("Received "+pathPrefix+id);
                if(ipAddress==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                NetworkInterface net = interfaceMap.get(id);
                if (net == null) {
                    log.error("Invalid interface id: " + id);
                    return ResponseStatusFactory.getResponseError(ResponseStatusFactory.STATUSCODE.INVALID_OPERATION);
                }
                net.setIPAddress(ipAddress);
                return ResponseStatusFactory.getResponseOK();
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                static public IPAddress get() throws Exception {
                    NetworkInterface net = IdResource.ExampleResource.get();
                    IPAddress address = net.getIPAddress();
                    address.setIpAddress(Wrappers.createStringCap("127.0.0.1"));
                    address.setSubnetMask(Wrappers.createStringCap("255.255.255.0"));
                    return address;
                }
            }
        }
    }
}
