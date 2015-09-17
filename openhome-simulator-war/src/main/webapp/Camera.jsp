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
    String registryGwURL = null;
    String serialNumber =  queryMap.getProperty("serialNo");
    String activationKey =  null;
    String xmppGW = null;
    String sharedSecret = queryMap.getProperty("sharedSecret");
    String motionDuration = queryMap.getProperty("motionDuration");

    if (serialNumber==null)
        serialNumber = simulatorFactory.getLastSerialNo();
    if (motionDuration==null)
        motionDuration = "20";

    // video soruces
    String videoSrcURL = queryMap.getProperty("videosrcurl");
    String videoSize = queryMap.getProperty("videosize");

    // process op options
    String op =  queryMap.getProperty("op");
    if (op != null) {
        if (op.equals("setsharedsecret")) {
            simulatorFactory.setSharedSecret(serialNumber, sharedSecret);
        } else if (op.equals("setrtsp")) {
            simulatorFactory.setRtspSourceURL(videoSize, videoSrcURL);
        } else if (op.equals("setmjpeg")) {
            simulatorFactory.setMjpegSourceURL(videoSize, videoSrcURL);
        } else if (op.equals("triggermotion")) {
            simulatorFactory.triggerMotion(serialNumber, motionDuration);
        }
    }

    if (sharedSecret==null)
        sharedSecret = simulatorFactory.getSharedSecret(serialNumber) ;
%>
<h2>OpenHome Camera Simulator Control Panel - Version 3.80</h2>

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
                    <th colspan="2" align="center">Camera Credentials</th>
                </tr>
                <tr>
                    <td>Serial Number</td>
                    <td><input type="text" name="serialNo" size="40" value="<%=serialNumber%>"/></td>
                </tr>
                <tr>
                    <td>SharedSecret</td>
                    <td><input type="text" name="sharedSecret" size="40" value="<%=sharedSecret%>"/></td>
                </tr>
                <tr>
                     <td align="center" colspan="2">
                            <input type="submit" name="setcredentials" value="Set" />
                            <input type="hidden" name="op" value="setsharedsecret"/>
                     </td>
                </tr>
            </table>
            </form>
        </td>
        <td valign="top">
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
