<%@ page import="com.icontrol.openhomesimulator.camera.CameraSimulatorFactory" %>
<%@ page import="com.icontrol.openhomesimulator.camera.CameraSimulator" %>
<%@ page import="org.slf4j.Logger" %>
<%@ include file="util.jsp" %>
<%--
    Document   : Camera Simulator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">



<html>
<HEAD>
  <SCRIPT language="JavaScript">
      function startup() {
      }
  </SCRIPT>
</HEAD>
<body onLoad="startup()">
<%
    CameraSimulatorFactory simulatorFactory = CameraSimulatorFactory.createInstance();
    simulatorFactory.setContextPath(request);
    Properties queryMap = parseQueryString(request);
    Logger log = (Logger) session.getAttribute("cameralog");

    // user input fields
    String registryGwURL = queryMap.getProperty("registryGwURL");
    String serialNumber =  queryMap.getProperty("serialNo");
    String activationKey =  queryMap.getProperty("activationKey");
    String xmppGW = queryMap.getProperty("xmppGwURL");
    String motionDuration = queryMap.getProperty("motionDuration");

    if (registryGwURL==null) {
        registryGwURL = simulatorFactory.getLastRegistryURL();
        if (registryGwURL==null) {
            String protocol = "http";
            if (request.getServerPort() == 8443) {
                protocol = "https";
            }
            //add "/rest" as [partner] part for rest framework api
            registryGwURL = protocol + "://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/gw/rest/registry";
        }
    }
    if (serialNumber==null) {
        serialNumber = simulatorFactory.getLastSerialNo();
    }
    if (activationKey==null)
        activationKey = "aabbccdd00";
    if (xmppGW==null)
        xmppGW = simulatorFactory.getXmppGw(serialNumber);
    if (motionDuration==null)
        motionDuration = "20";

    // video soruces
    String videoSrcURL = queryMap.getProperty("videosrcurl");
    String videoSize = queryMap.getProperty("videosize");

    // process op options
    String op =  queryMap.getProperty("op");
    if (op != null) {
        if (op.equals("startbootstrap")) {
            simulatorFactory.startBootStrap(registryGwURL, serialNumber, activationKey, log);
            // get xmpp address
            xmppGW = simulatorFactory.getXmppGw(serialNumber);
        } else if (op.equals("setrtsp")) {
            simulatorFactory.setRtspSourceURL(videoSize, videoSrcURL);
        } else if (op.equals("setmjpeg")) {
            simulatorFactory.setMjpegSourceURL(videoSize, videoSrcURL);
        } else if (op.equals("xmppconnect")) {
            simulatorFactory.xmppConnect(serialNumber, xmppGW);
        } else if (op.equals("xmppdisconnect")) {
            simulatorFactory.xmppDisconnect(serialNumber);
        } else if (op.equals("triggermotion")) {
            simulatorFactory.triggerMotion(serialNumber, motionDuration);
        }
    }
    // gather camera parameters
    String bootstrapStatus = simulatorFactory.getStatus(serialNumber);
    String siteID = simulatorFactory.getSiteID(serialNumber);
    String sharedSecret = simulatorFactory.getSharedSecret(serialNumber);
    String sessionGW = simulatorFactory.getSessionGw(serialNumber);
    boolean isXmppConnected = simulatorFactory.isXMPPconnected(serialNumber);
%>
<h2>Camera Simulator Control Panel - Version 3.8</h2>

<a href="index.jsp" target="_blank">Home</a>&nbsp;&nbsp;&nbsp;
<a href="camera">Refresh This Page</a>
&nbsp;&nbsp;&nbsp;
<br><br>
<table>
    <tbody>
    <tr>
        <td>
            <form name="F1" action="" method="POST">
            <table border="1">
                <tr>
                    <th colspan="2" align="center">Bootstrap Process - <%=bootstrapStatus%></th>
                </tr>
                <tr>
                    <td>Registry Gateway URL</td>
                    <td><input type="text" name="registryGwURL" size="40" value="<%=registryGwURL%>"/></td>
                </tr>
                <tr>
                    <td>Serial Number</td>
                    <td><input type="text" name="serialNo" size="40" value="<%=serialNumber%>"/></td>
                </tr>
                <tr>
                    <td>Activation Key</td>
                    <td><input type="text" name="activationKey" size="40" value="<%=activationKey%>"/></td>
                </tr>
                <tr>
                    <td>SiteID</td>
                    <td><%=siteID%></td>
                </tr>
                <tr>
                    <td>SharedSecret</td>
                    <td><%=sharedSecret%></td>
                </tr>
                <tr>
                    <td>Session Gateway</td>
                    <td><%=sessionGW%></td>
                </tr>
                <tr>
                     <td align="center" colspan="2">
                            <input type="submit" name="start" value="<%=simulatorFactory.finishedBootStrap(serialNumber)?"Restart":"Start"%>" />
                            <input type="hidden" name="op" value="startbootstrap"/>
                     </td>
                </tr>
            </table>
            </form>
        </td>
        <td>
            <table border="1">
                <tr>
                    <th colspan="3" align="center">XMPP Connection Status</th>
                </tr>
                <tr>
                    <%if (isXmppConnected) { %>
                        <td colspan="3" align="center">Connected</td>
                    <% } else { %>
                        <td colspan="3" align="center">Not Connected</td>
                    <% } %>
                </tr>
                <tr>
                    <form name="F1" action="" method="POST">
                    <td>
                        XMPP Gateway URL
                    </td>
                    <td>
                        <input type="text" name="xmppGwURL" size="42" value="<%=xmppGW%>"/>
                    </td>
                    <td align="center">
                        <%if (isXmppConnected) { %>
                                <input type="submit" name="Disconnect" value="Disconnect" />
                                <input type="hidden" name="op" value="xmppdisconnect"/>
                                <input type="hidden" name="serialNo" value="<%=serialNumber%>"/>
                            </form>
                        <% } else { %>
                            <form name="F1" action="" method="POST">
                                <input type="submit" name="Connect" value="Connect"/>
                                <input type="hidden" name="op" value="xmppconnect"/>
                                <input type="hidden" name="serialNo" value="<%=serialNumber%>"/>
                        <% } %>
                    </td>
                    </form>
                </tr>
            </table>
            <br>
            <table border="1">
                <tr>
                    <th colspan="3" align="center">Live Video URLs</th>
                </tr>
                <tr>
                    <form name="F1" action="" method="POST">
                        <td>
                            RTSP VGA URL
                        </td>
                        <td>
                            <input type="text" name="videosrcurl" size="50" value="<%=simulatorFactory.getRtspSourceURL("640x480")%>"/>
                        </td>
                        <td>
                            <input type="submit" name="setvgartsp" value="Set"/>
                            <input type="hidden" name="videosize" value="640x480"/>
                            <input type="hidden" name="op" value="setrtsp"/>
                        </td>
                    </form>
                </tr>
                <tr>
                    <form name="F1" action="" method="POST">
                        <td>
                            RTSP QVGA URL
                        </td>
                        <td>
                            <input type="text" name="videosrcurl" size="50" value="<%=simulatorFactory.getRtspSourceURL("320x240")%>"/>
                        </td>
                        <td>
                            <input type="submit" name="setqvgartsp" value="Set"/>
                            <input type="hidden" name="videosize" value="320x240"/>
                            <input type="hidden" name="op" value="setrtsp"/>
                        </td>
                    </form>
                </tr>
                <tr>
                    <form name="F1" action="" method="POST">
                        <td>
                            MJPEG VGA URL
                        </td>
                        <td>
                            <input type="text" name="videosrcurl" size="50" value="disabled" disabled="disabled"/>
                        </td>
                        <td>
                            <input type="submit" name="vgamjpeg" value="Set" disabled="disabled"/>
                            <input type="hidden" name="videosize" value="640x480"/>
                            <input type="hidden" name="op" value="setmjpeg"/>
                        </td>
                    </form>
                </tr>
            </table>
        </td>
    </tr>
    <br>
    <br>
    <br>
    <br>
    <br>
    <br>
    <tr>
        <table border="1">
            <tr>
                <th colspan="3" align="center">Motion Detection Trigger</th>
            </tr>
            <tr>
                <form name="F1" action="" method="POST">
                    <td>
                        Motion Duration (sec)
                    </td>
                    <td>
                        <input type="text" name="motionduration" size="20" value="<%=motionDuration%>"/>
                    </td>
                    <td>
                        <input type="submit" name="trigger" value="Trigger"/>
                        <input type="hidden" name="op" value="triggermotion"/>
                    </td>
                </form>
            </tr>
        </table>
    </tr>
    </tbody>
</table>
<br><br>

</body>
</html>
