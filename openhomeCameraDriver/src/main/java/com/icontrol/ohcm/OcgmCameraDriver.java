package com.icontrol.ohcm;


import java.net.URL;

import com.icontrol.openhome.data.CreateMediaTunnel;
import com.icontrol.openhome.data.FirmwareDownload;
import com.icontrol.openhome.data.MediaUpload;
import com.icontrol.openhome.data.ResponseStatus;

public interface OcgmCameraDriver {

    /**
     * Gets location URL to physical camera.
     * 
     * @return URL - URL of device.
     */
    public URL getLocationURL();

    /**
     * Set the physical camera location URL.
     * @param locationURL The camera URL.
     */
    public void setLocationURL(URL locationURL);

    /**
     * Sets viewer user
     *
     * @param viewUser
     */
    public void setAdminUser(String viewUser);

    /**
     * Set viewer password
     *
     * @param viewPassword
     */
    public void setAdminPassword(String viewPassword);

    /**
     * Returns root user
     *
     * @return
     */
    public String getDefaultRootUser();

    /**
     * Returns root password
     *
     * @return
     */
    public String getDefaultRootPassword();

    /**
     * Returns viewer user
     *
     * @return
     */
    public String getAdminUser();

    /**
     * Returns viewer password
     *
     * @return
     */
    public String getAdminPassword();

    /**
     * Set connect timeout.
     *
     * @param connectTimeout - connection timeout in milliseconds
     */
    public void setConnectTimeout(int connectTimeout);

    /**
     * Get the connection time value.
     * 
     * @return - timeout in milliseconds
     */
    public int getConnectTimeout();

    /**
     * Set read timeout
     *
     * @param readTimeout
     */
    public void setReadTimeout(int readTimeout);

    /**
     * @return read timeout
     */
    public int getReadTimeout();

    /**
     * Set firmware upgrade timeout
     *
     * @param fwUpgradeTimeout
     */
    public void setFirmwareUpgradeReadTimeout(int fwUpgradeTimeout);

    /**
     * @return firmware upgrade timeout
     */
    public int getFirmwareUpgradeReadTimeout();

    /**
     * Set clip upload timeout
     *
     * @param clipUploadTimeout
     */
    public void setClipUploadReadTimeout(int clipUploadTimeout);

    /**
     * @return clip upload timeout
     */
    public int getClipUploadReadTimeout();

    /**
     * Returns true if camera meets all "alive" conditions
     *
     * @return
     * @throws CameraException
     */
    public boolean isAlive() throws CameraException;

    /**
     * Returns version of the firmware installed
     *
     * @return
     * @throws CameraException
     */
    public String getFirmwareVersion() throws CameraException;
    
    /**
     * Upgrades firmware from remote URL
     *
     * @param fwUpgradeURL - URL
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus upgradeFirmwareFrom(URL fwUpgradeURL) throws CameraException;

    /**
     * Upgrades firmware from remote URL specified in FirmwareDownload.
     * 
     * @param firmwareDownload - FirmwareDownload
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus upgradeFirmware(FirmwareDownload firmwareDownload) throws CameraException;

    /**
     * Returns logs since last reboot/factory reset
     *
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus getLogs() throws CameraException;

    /**
     * Returns signal strength
     *
     * @return ResponseStatus
     * @throws CameraException
     */
    public Integer getSignalStrength() throws CameraException;
    
    /**
     * Returns signal strength for the specified Network Interface.
     *
     * @return ResponseStatus
     * @throws CameraException
     */
    public Integer getSignalStrength(String networkInterfaceId) throws CameraException;
     
    /**
     * Start a live video stream to the URL specified in the CreateMediaTunnel object.
     * 
     * @param uploadURL - CreateMediaTunnel
     * @return  ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus postLiveVideo(CreateMediaTunnel createMediaTunnel) throws CameraException;

    /**
     * Get a picture image from the camera.
     * 
     * @param videoResolutionWidth - picture width in pixels.
     * @param videoResolutionHeight - picture height in pixels.
     * @return JPEG image - image is returned as a byte stream.
     * @throws CameraException
     */
    public byte[] snapshot(String videoResolutionWidth, String videoResolutionHeight, String fixedQuality) throws CameraException;
    
    /**
     *  Get a picture image from the default channel.
     *  
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus getSnapshot() throws CameraException;
    
    /**
     * Get a picture image from the specified channel.
     * 
     * @param id - streaming channel id.
     * @param videoResolutionWidth
     * @param videoResolutionHeight
     * @param fixedQuality
     * @return
     * @throws CameraException
     */
    public ResponseStatus getSnapshot(String id, String videoResolutionWidth, String videoResolutionHeight, String fixedQuality) throws CameraException;
    
    /**
     * Request a snapshot be posted to a URL from the default streaming channel.
     * 
     * @param mediaUpload - MediaUpload
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus postSnapshot(MediaUpload mediaUpload) throws CameraException;
    
    /**
     * Request a snapshot be posted to a URL from the specified streaming channel.
     * 
     * @param mediaUpload - MediaUpload object 
     * @param channelId - id of streaming channel
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus postSnapshot(MediaUpload mediaUpload, String channelId) throws CameraException;
    
    /**
     * Request a video clip be posted to a URL from the default streaming channel.
     * 
     * @param mediaUpload - MediaUpload
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus postVideoClip(MediaUpload mediaUpload) throws CameraException;
    
    /**
     * Request a video clip be posted to a URL from the specified streaming channel.
     * 
     * @param mediaUpload - MediaUpload object 
     * @param channelId - id of streaming channel
     * @return
     * @throws CameraException
     */
    public ResponseStatus postVideoClip(MediaUpload mediaUpload, String channelId) throws CameraException;

    /**
     * Get PTZ configuration for all channels.
     * 
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus getPTZConfiguration() throws CameraException;
    
    /**
     * Get PTZ configuration for the specified channel.
     * 
     * @param channelId - PTZ channel Id.
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus getPTZConfiguration(String channelId) throws CameraException;
    
    /**
     * Get the current absolute position of the camera.
     * 
     * @param channelId - PTZ channel Id.
     * @return ResponseStatus (PTZStatus)
     * @throws CameraException
     */
    public ResponseStatus getCurrentPosition(String channelId) throws CameraException;
    
    /**
     * Set the home position of the PTZ camera to the current camera position.
     * 
     * @param channelId - PTZ channel Id.
     * @return  ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setHomePosition(String channelId) throws CameraException;
    
    /**
     * Pans, tilts, and/or zooms the device relative to the current position.
     * 
     * @param channelId - PTZ channel Id.
     * @param pan - pan the camera left (negative) or right (positive) the number of specified degrees.
     * @param tilt - tilt the camera down (negative) or up (positive) the number of specified degrees.
     * @param zoom - adjust the zoom in (positive) or out (negative) the specified percentage (-100 through 100).
     * @return ResponseStatus
     * @throws CameraException
     */
    public ResponseStatus setPanTiltZoomRelative(String channelId, String pan, String tilt, String zoom) throws CameraException;
    
    /**
     * Pans, tilts, and/or zooms the device to the absolute position specified.
     * 
     * @param channelId - PTZ channel Id.
     * @param pan - pan the camera left or right to the absolute azimuth specified (0 through 360).
     * @param tilt - tilt the camera up or down to the absolute elevation specified (-90 through 90).
     * @param zoom - adjust the zoom to the absolute percentage specified (0-100).
     * @return
     * @throws CameraException
     */
    public ResponseStatus setPanTiltZoomAbsolute(String channelId, String pan, String tilt, String zoom) throws CameraException;

    
}
