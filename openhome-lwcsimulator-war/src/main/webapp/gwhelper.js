/*

File: gwhelper.js

*/


function checkJavaQTversion()
{
    try
    {
        var jvmVer = PluginDetect.isMinVersion('Java', '1.5', "getJavaInfo.jar");
        if (jvmVer == -2)
            throw "ActiveX Disabled. Please Enable ActiveX before Continuing.";
        else if (jvmVer < 1)
            throw "You must upgrade to the latest version of Java before you\r\ncan view live video.";
    }
    catch (err1)
    {
        alert(err1);
        return false;
    }
    try
    {
        var qtVer = PluginDetect.isMinVersion('QuickTime', '7,2')
        if (qtVer != 1)
            throw "You must upgrade to the latest version of QuickTime before you\r\ncan view live video.";
    }
    catch(err)
    {
        alert(err);
        return false;
    }
    return true;
}
