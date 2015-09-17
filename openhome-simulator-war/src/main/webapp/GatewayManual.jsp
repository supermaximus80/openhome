<%@ page import="com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory" %>
<%@ page import="com.icontrol.openhomesimulator.camera.RtspURL" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.IOException" %>
<%@ include file="util.jsp" %>
<%@ page import="com.icontrol.openhomesimulator.util.Utilities" %>

<%--
    Document   : Gateway Simulator
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">


<html>
<HEAD>
<SCRIPT language="JavaScript" src="AC_QuickTime.js"></SCRIPT>
<SCRIPT src="plugindetect.js" language="javascript" type="text/javascript"></SCRIPT>
<SCRIPT src="gwhelper.js" language="javascript" type="text/javascript"></SCRIPT>
<SCRIPT language="JavaScript">
// path and camera current selection variables
var checkID = null;
var curPathSelect = null;
var curCameraSelect = null;
// called when user changes path
function onSelection(selectObj) {
    // get the index of the selected option
    var idx = selectObj.selectedIndex;
    // get the value of the selected option
    var which = selectObj.options[idx].value;

    var newURL = "gateway?pathselect="+which+"&cameraselect="+curCameraSelect;
    window.location.replace(newURL);
}
// called when user changes path or gateway address selection
function onCameraSelection(selectObj) {
    // get the index of the selected option
    var idx = selectObj.selectedIndex;
    // get the value of the selected option
    var which = selectObj.options[idx].value;

    var newURL = "gateway?cameraselect="+which+"&pathselect="+curPathSelect;
    window.location.replace(newURL);
}
// poll for user notifications
var retriesLeft = -1;
var timeOutMessage = "Timeout waiting for Camera";
function startEventPoll(curCheckID, curPath, curCamera) {
    checkID = curCheckID;
    curPathSelect = curPath;
    curCameraSelect = curCamera;
    tick();
}
function setEventPollWait(numRetries, timeoutMsg) {
    retriesLeft = numRetries;
    timeOutMessage = timeoutMsg;
}
var xhr;
function tick() {
    if (retriesLeft-- == 0) {
        alert("Timed out waiting for camera");
    }
    xhr = new XMLHttpRequest();
    xhr.onreadystatechange = tock;
    xhr.open("GET", "gw/upload/hasnew?id="+checkID, true);
    xhr.send(null);
}
function tock() {
    if (xhr.readyState == 4) {
        var status = xhr.status;
        if (status == 200) {
            var respStr = xhr.responseText;
            if (respStr != null && respStr.length > 0) {
                var body = getBody(respStr);
                var type = getMediaType(respStr);
                if (type=="image") {
                    alert("New "+type+" available for viewing.") ;
                    document.getElementById( "media_div" ).innerHTML = generateImageObjectEmbedTag( body ) ;
                } else if (type=="video") {
                    alert("New "+type+" available for viewing.") ;
                    document.getElementById( "media_div" ).innerHTML = generateQTVideoClipEmbedTag( body ) ;
                } else if (type=="alert") {
                    alert(body);
                    document.getElementById( "media_div" ).innerHTML = body ;
                } else if (type=="text") {
                    document.getElementById( "media_div" ).innerHTML = body ;
                } else if (type=="<%=GatewaySimulatorFactory.ALERT_CONNECTION%>") {
                    alert("New XMPP Connection from "+body);
                    var newURL = "gateway?pathselect="+curPathSelect+"&cameraselect="+body;
                    window.location.replace(newURL);
                } else if (type=="<%=GatewaySimulatorFactory.ALERT_DISCONNECT%>") {
                    alert("XMPP Client disconnected. address="+body);
                    var newURL = "gateway?pathselect="+curPathSelect;
                    window.location.replace(newURL);
                }
                window.scroll(0,findPos(document.getElementById( "media_div" )));
                // reset timeout
                retriesLeft = -1;
            }
        }
        // continue
        setTimeout("tick()", 500);
    }
}
function startRTSP(rtspUrl, host, port, authUser, authPassword) {
    //if (!checkJavaQTversion())
    //    return;

    document.getElementById( "media_div" ).innerHTML = GenerateRtspPanel("0", rtspUrl, host, port, authUser, authPassword);
    window.scroll(0,findPos(document.getElementById( "media_div" )));

}
function getBody(str) {
    var pos = str.indexOf("&");
    if (pos < 0)
        return null;
    return str.substr(0, pos);
}
function getMediaType(str) {
    var pos = str.indexOf("&");
    if (pos < 0)
        return null;
    return str.substr(pos+1);
}
function generateImageObjectEmbedTag( iImageURL )
{
    var embedQT = "Uploaded Image <br>";
    embedQT += "<img src=" + iImageURL + " border=\"0\" alt=\"Waiting for Image upload\" /> ";
    return embedQT;
}
function findPos(obj)
{
    var curtop = 0;
    if (obj.offsetParent) {
        do {
            curtop += obj.offsetTop;
        } while (obj = obj.offsetParent);
        return curtop;
    }
}
function GenerateRtspPanel(id, rtspUrl, host, port)
{
    // movie div
    var movieid = "movie" + id;
    var rtspName = rtspUrl + " :: " + host + ":" + port;
    var xhtml = "Live Video<br>";
    xhtml += '<div style="padding-bottom: 0px;" id="'+movieid+'">';
    xhtml += '<img src="initializing-640.gif" width="640" height="495" border="0" alt="Initializing..."/>';
    xhtml += '</div>';
//    xhtml += '<tr><td><font color="black" id="statusbar">Connecting to Camera...</font></td></tr>';
    xhtml += '<tr><td><font color="black" id="statusbar">Connecting to Camera '+rtspName+' ...</font></td></tr>';

    xhtml += '<div id="appletdiv">';
    xhtml += GenAppletObject("0", rtspUrl, host, port);
    xhtml += '</div>';
    return xhtml;
}
function GenAppletObject(id, rtspUrl, host, port, authUser, authPassword)
{
    var appname = "icApplet";
    var width = "320";
    var height = "1";
    var xhtml = '<applet name="'+appname+'" archive="ica.jar" code="com.icontrol.applets.bridgeapplet.BridgeApplet.class" width="'+width+'" height="'+height+'" mayscript="true">';
    xhtml += ' <param name="url" value="'+rtspUrl+'"/>';
    xhtml += ' <param name="use_get" value="false"/> ';
    xhtml += ' <param name="server_ip" value="'+host+'"/>';
    xhtml += ' <param name="server_port" value="'+port+'"/>';
    xhtml += ' <param name="auth_user" value="'+authUser+'"/>';
    xhtml += ' <param name="auth_password" value="'+authPassword+'"/>';
    xhtml += ' <param name="osxsafari" value="true"/>';
    xhtml += ' <param name="logging" value="on"/>';
    xhtml += ' <param name="notifystatus" value="statusbar"/>';
    xhtml += ' <param name="notifyerror" value="movie'+id+'"/>';
    xhtml += ' <param name="movid" value="0"/>';
    xhtml += ' </applet>';
    return xhtml;
}
function showQuickTime(src, divname)
{
    d = document.getElementById(divname);
    if (d == null)
        return;
    var xhtml = QT_GenerateOBJECTText_XHTML("connecting-320.mov", 640, 495, '',
            "AUTOPLAY", "true",
            "CONTROLLER", "true",
            "SHOWLOGO", "false",
            'qtsrc', src) ;
    d.innerHTML = xhtml;
}
function AppletStatusUpdate(divname, sStatus)
{
    d = document.getElementById(divname);
    if (d != null)
    {
        document.getElementById(divname).innerHTML = TranslateStatusCode(divname, sStatus);
    }
}

function AppletError(divname, sError)
{
    d = document.getElementById(divname);
    if (d == null)
        return;

    var html = '<br><br><br><table align=center cellpadding=0 cellspacing=0><tr><td>';
    html += sError;
    html += '</td></tr></table><br><br><br><br><br></div>';

    d.innerHTML = html;
}

function TranslateStatusCode(divID, sStatus)
{
    if (sStatus.match("#err1") != null)	// Video connect error: Click to change settings.
    {
        return ('Video connectivity error: ');
    }
    else
        return sStatus;
}
function generateQTVideoClipEmbedTag( iVideoURL )
{
    var embedQT = "Uploaded Video<br>";
    try
    {
        embedQT += QT_GenerateOBJECTText_XHTML( "connecting-320.mov", 640, 495, "",
                "CONTROLLER", "true",
                'qtsrc', iVideoURL,
                "SHOWLOGO", "false",
                "AUTOPLAY", "true" );
    }
    catch( e )
    {
        alert( "QuickTime plug-in is not available." );
    }
    return( embedQT );
}
</SCRIPT>
</HEAD>
<body>
<%
    int CHECK_EVENT_INTERVAL = 500;        // 500 ms
    int MEDIAUPLOAD_TIMEOUT = 25000 ;      // 25 sec

    Properties queryMap = parseQueryString(request);
    GatewaySimulatorFactory simulator = GatewaySimulatorFactory.getInstance();
    simulator.setContextPath(request);
    simulator.initCustomEventResource();
    StringBuilder responseSB = new StringBuilder();
    int responseCode = -1;
    String selectedURI =  queryMap.getProperty("pathselect");
    String cameraSelection = queryMap.getProperty("cameraselect");
    String cameraURL = queryMap.getProperty("cameraurl");            // should be set only when op is not null
    String apiURI =  queryMap.getProperty("apiuri");                 // should be set only when op is not null
    String accessMethod = queryMap.getProperty("accessMethod");
    String adminUserName = queryMap.getProperty("adminusername");
    String adminPassword = queryMap.getProperty("adminpassword");
    String manualInputCameraURL = queryMap.getProperty("manualInputCameraURL");
    String requestBody = queryMap.getProperty("requestXML");
    String id = queryMap.getProperty("id");
    id = (id == null) ? "0" : id;

    boolean bMediaUploadWaiting = false;
    RtspURL rtspURL = null;
    String httpURL = null;

    try {
        // determine if manual camera url
        if (simulator.getManualCameraURL().equals(cameraSelection) || simulator.getManualCameraURL().equals(cameraURL))
            cameraURL = manualInputCameraURL;
        //System.out.println("cameraSelection="+cameraSelection+" manualInputCameraURL="+manualInputCameraURL+ " cameraURL="+cameraURL);
        // camera selection
        if (cameraSelection==null) {
            cameraSelection = simulator.getManualCameraURL();
        }
        // setup example url, if needed
        if (manualInputCameraURL==null) {
            if (simulator.getLastCameraURL() != null)
                manualInputCameraURL = simulator.getLastCameraURL();
            else  {
                String protocol = "http";
                if (request.getServerPort() == 8443) {
                    protocol = "https";
                }
                //add "/rest" as [partner] part of rest framework api
                manualInputCameraURL = protocol + "://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/cam";
            }
        }

        // determine apiURI
        if (simulator.getManualPathName().equals(selectedURI) || simulator.getManualPathName().equals(apiURI)) {
            apiURI = queryMap.getProperty("manualInputURI");
        }
        if (selectedURI==null) {
            selectedURI = "/OpenHome/Security/AAA/accounts";
        }

        // admin username and password
        if (adminUserName == null) {
            if (simulator.getLastUsername() != null)
                adminUserName = simulator.getLastUsername() ;
            else
                adminUserName = "administrator";
        }
        if (adminPassword == null) {
            if (simulator.getLastPassword() != null)
                adminPassword = simulator.getLastPassword();
            else
                adminPassword = "";
        }

        // get op
        String op =  queryMap.getProperty("op");
        // request body
        if (accessMethod != null && (accessMethod.equalsIgnoreCase("GET") || accessMethod.equalsIgnoreCase("ERASE"))) {
            requestBody = null;
        }
        if (requestBody != null)
            requestBody = requestBody.trim();
        else {
            if (op == null)
                requestBody = simulator.getExampleRequestXML(selectedURI);
            else
                requestBody = "";
        }
        // do the request
        if (op != null && op.equals("startTest")) {
            if (cameraURL != null && apiURI != null && accessMethod != null) {
                apiURI = apiURI.trim();
                if (apiURI.contains("http/mjpg") && accessMethod.equals("GET")){
                    httpURL = cameraURL+apiURI;
                }
                else{
                    StringBuilder rtspUrlSB = new StringBuilder();
                    if (apiURI.indexOf("[UID]") != -1) {
                        apiURI = apiURI.replaceAll("\\[UID\\]", id);
                    }
                    responseCode = simulator.processRequest(accessMethod, apiURI, cameraURL, responseSB, requestBody, rtspUrlSB, adminUserName, adminPassword);
                    if (responseCode == GatewaySimulatorFactory.RTSP_ACTION_CODE) {
                        rtspURL = new RtspURL(rtspUrlSB.toString()+"?channel="+id);
                    }
                    else {
                        bMediaUploadWaiting = simulator.isUploadMedia(apiURI) ;
                    }
                }
            }
        }
    } catch (Exception ex) {
        System.out.println("GatewayManual.jsp caught "+ex);
        throw new Exception("GatewayManual.jsp caught "+ex,ex) ;
    }
%>

<h2>OpenHome Gateway Simulator Control Panel - Version 3.80</h2>

<a href="index.jsp" target="_blank">Home</a>&nbsp;&nbsp;&nbsp;
<a href=<%="gateway?pathselect="+selectedURI+"&cameraselect="+cameraSelection%>>Refresh This Page</a>
&nbsp;&nbsp;&nbsp;
<br>
<form name="F1" method="POST" action="">
    <table border="1">
        <tr>
            <th colspan="4">Camera Info</th>
        </tr>
        <tr>
            <td>Camera Address</td>
            <td>
                <select onChange="onCameraSelection(this);" name="cameraurl">
                    <%
                        Iterator<String> iterXMPP = simulator.getXmppClientSessions();
                        while (iterXMPP.hasNext()) {
                            String cameraAddress = iterXMPP.next();
                            if (cameraAddress.equalsIgnoreCase(cameraSelection)) { %>
                    <option selected="selected" value="<%=cameraAddress%>"><%=cameraAddress%></option>
                    <% } else {  %>
                    <option value="<%=cameraAddress%>"><%=cameraAddress%></option>
                    <%} %>
                    <%}
                        boolean bManualCameraURL = false;
                        if (cameraSelection.equalsIgnoreCase(simulator.getManualCameraURL()))
                            bManualCameraURL = true;
                    %>
                    <option value="<%=simulator.getManualCameraURL()%>" <%=bManualCameraURL?"selected=\"selected\"":"" %> ><%=simulator.getManualCameraURL()%></option>
                </select>
            </td>
            <td>Admin Username</td>
            <td><input type="text" name="adminusername" size="20" value="<%=adminUserName%>"/></td>
        </tr>
        <tr>
            <% if (bManualCameraURL) { %>
            <td>Enter Camera URL</td>
            <td><input type="text" name="manualInputCameraURL" size="50" value="<%=manualInputCameraURL%>" <%=bManualCameraURL?"":"disabled=\"disabled\""%>/></td>
            <% } else { %>
            <td>XMPP Connection</td>
            <% if (simulator.isXmppCnnected(cameraSelection)) { %>
            <td> Connected </td>
            <% } else { %>
            <td> Disconnected </td>
            <% } %>
            <%}%>
            <td>Admin Password</td>
            <td><input type="text" name="adminpassword" size="20" value="<%=adminPassword%>"/></td>
        </tr>
    </table>
    <br>
    <table border="1">
        <tr>
            <th colspan="4">OpenHome API</th>
        </tr>
        <tr>
            <td>OpenHome API URI</td>
            <td>
                <select onChange="onSelection(this);" name="apiuri">
                    <%  Iterator<String> iter = simulator.getAPIpaths();
                        while (iter.hasNext()) {
                            String path = iter.next().replace("[partner]","/OpenHome").replace("[NOTIFYID]","0");
                            if (path.equalsIgnoreCase(selectedURI)) {   %>
                    <option selected="selected" value="<%=path%>"><%=path%></option>
                    <% } else {  %>
                    <option value="<%=path%>"><%=path%></option>
                    <%} %>
                    <%} %>
                    <option value="<%=simulator.getManualPathName()%>" <%=simulator.getManualPathName().equalsIgnoreCase(selectedURI)?"selected=\"selected\"":"" %> ><%=simulator.getManualPathName()%></option>
                </select>
            </td>
            <td>ID</td>
            <td><input type="text" name="id" size="3" value="<%=id%>"/></td>
        </tr>
        <tr>
            <td>Manual URI entry</td>
            <td><input colspan="3" type="text" name="manualInputURI" size="50" value="<%=simulator.getPAthPrefix()%>" <%=simulator.getManualPathName().equalsIgnoreCase(selectedURI)?"":"disabled=\"disabled\""%>/></td>
        </tr>
        <tr>
            <td align="center" colspan="2">
                <input type="submit" width="30" align="center" name="accessMethod" <%=simulator.supportMethodForPath("GET",selectedURI)?"":"disabled=\"disabled\""%> value="GET">
                <input type="submit" width="30" align="center" name="accessMethod" <%=simulator.supportMethodForPath("PUT",selectedURI)?"":"disabled=\"disabled\""%> value="PUT">
                <input type="submit" width="30" align="center" name="accessMethod" <%=simulator.supportMethodForPath("POST",selectedURI)?"":"disabled=\"disabled\""%> value="POST">
                <input type="submit" width="30" align="center" name="accessMethod" <%=simulator.supportMethodForPath("DELETE",selectedURI)?"":"disabled=\"disabled\""%> value="DELETE">
            </td>
        </tr>
    </table>
    <br>
    <table>
        <tr>
            <td align="left">Request path: <%=selectedURI%></td>
        </tr>
        <tr>
            <td><textarea text-align="left" name="requestXML" id="requestXML" rows="10" cols="80"><%=requestBody%></textarea></td>
        </tr>
    </table>
    <input type="hidden" name="op" value="startTest"/>
</form>
<br>
<table>
    <tr>
        <td align="left">
            <% if (responseCode >= 0) { %>
            Response: HTTP Status Code <%=responseCode  %>
            <%} else { %>
            Response:
            <%}%>
        </td>
    </tr>
    <tr>
        <td>
            <%  String resp = "";
                if (responseCode == 401) {
                    resp = "Unauthorized access request.  Use correct authentication credentials.";
                } else if (responseCode == 404) {
                    resp = "Unsupported or unimplemented request.";
                } else if (responseCode == 200) {
                    resp = Utilities.prettyFormat(responseSB.toString());
                } else if (responseCode >= 0) {
                    resp = "Failed request.  Refer to logs for details.";
                }
            %>
            <textarea name="responseXML" id="responseXML" rows="10" cols="80"><%=resp%></textarea>
        </td>
    </tr>
</table>
<br>
<div id="media_div" >
</div>
<SCRIPT language="JavaScript">
    startEventPoll("<%=simulator.getCheckEvent()%>","<%=selectedURI%>", "<%=cameraSelection%>");
</SCRIPT>
<% if (bMediaUploadWaiting) { %>
<SCRIPT language="JavaScript">
    setEventPollWait(<%=MEDIAUPLOAD_TIMEOUT/CHECK_EVENT_INTERVAL%>, "Timeout Waiting for Media Upload");
</SCRIPT>
<% } else if (rtspURL != null) {%>
<SCRIPT language="JavaScript">
    setTimeout( function(){startRTSP( "<%=rtspURL.encode()%>", "<%=rtspURL.getHost()%>", "<%=rtspURL.getPort()%>", "<%=adminUserName%>", "<%=adminPassword%>" )}, 20 );
</SCRIPT>
<%} else if (httpURL != null){%>
<script type="text/javascript">
    window.open ("<%=httpURL%>","_self",false);
</script>
<%}%>
</body>
</html>
