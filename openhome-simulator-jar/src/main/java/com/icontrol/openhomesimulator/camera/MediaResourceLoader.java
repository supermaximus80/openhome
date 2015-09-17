package com.icontrol.openhomesimulator.camera;

import com.icontrol.openhome.data.MediaUpload;
import com.icontrol.openhome.data.VideoClipFormatType;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MediaResourceLoader {
    public enum ImageSize {QQVGA, QVGA, VGA};


    private static final Logger log = LoggerFactory.getLogger(MediaResourceLoader.class);

    static int MAX_MEDIA_SIZE = 5*1024*1024;

    static Map<String, String> imageSizeToFileNameMap;
    static Map<String, String> videoSizeToFileNameMap;

    static {
        imageSizeToFileNameMap = new HashMap<String, String>() ;
        imageSizeToFileNameMap.put("640x480_image", "resources/image_vga.jpg") ;
        imageSizeToFileNameMap.put("320x240_image", "resources/image_qvga.jpg") ;

        videoSizeToFileNameMap = new HashMap<String, String>() ;
        videoSizeToFileNameMap.put("640x480_video", "resources/video_vga.mp4") ;
        videoSizeToFileNameMap.put("320x240_video", "resources/video_qvga.mp4") ;
        videoSizeToFileNameMap.put("test_video","resources/VID_20121023_132321.mp4");
    }

    /*
      loadImage
     */
    public static byte[] loadImage(int width, int height) throws IOException {
        String key = Integer.toString(width) + "x" + Integer.toString(height) + "_image";
        String filename = imageSizeToFileNameMap.get(key);
        if (filename == null)
            throw new IOException("Unable to find image size:"+key);
        return loadFile(filename) ;
    }

    /*
      loadVideo
     */
    public static byte[] loadVideo(int width, int height, VideoClipFormatType type) throws IOException {
        String key = Integer.toString(width) + "x" + Integer.toString(height) + "_video";
        String filename = videoSizeToFileNameMap.get(key);
        if (filename == null)
            throw new IOException("Unable to find video size:"+key);
        return loadFile(filename) ;
    }

    public static byte[] loadTestVideo() throws IOException {
        return loadFile("resources/video_vga.mp4");
    }

    /*
      loadFile
     */
    public static byte[] loadFile(String filename) throws IOException {
        InputStream is = null;
        try {
            if (filename.startsWith("resources/")) {
                is = MediaResourceLoader.class.getResourceAsStream(filename);
                if (is == null)
                    throw new IOException("can't find resource " + filename);
            } else {
                File file = new File(filename);
                is = new FileInputStream(file);
            }

            byte[] buffer = new byte[MAX_MEDIA_SIZE];
            int r;
            int offset = 0;
            while (offset < buffer.length && (r = is.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += r;
            }
            is.close();
            byte[] ret = new byte[offset];
            System.arraycopy(buffer, 0, ret, 0, offset);
            //log.debug("loadImage successfully read " + offset + " bytes");
            return ret;
        } catch (Exception ex) {
            log.error("loadImage caught " + ex);
            throw new IOException("loadImage caught " + ex);
        }
    }

}
