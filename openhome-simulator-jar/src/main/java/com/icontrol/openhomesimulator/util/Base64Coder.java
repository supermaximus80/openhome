package com.icontrol.openhomesimulator.util;

import org.bouncycastle.util.encoders.Base64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author  aaron
 */

/*************************************************************************
 *
 * This class is a wrapper around the Bouncy Castle Base64 encoder.
 *
 **************************************************************************/

public class Base64Coder
{

    /**
     * Encodes a string into Base64 format.
     * No blanks or line breaks are inserted.
     * @param s  a String to be encoded.
     * @return   A String with the Base64 encoded data.
     */
    public static String encode (String s) {
        return new String(encode(s.getBytes())); }

    /**
     * Encodes a byte array into Base64 format.
     * No blanks or line breaks are inserted.
     * @param in  an array containing the data bytes to be encoded.
     * @return    A byte array with the Base64 encoded data.
     */
    public static byte[] encode (byte[] in)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
        try {
            Base64Encoder encoder = new Base64Encoder();
            encoder.encode(in, 0, in.length, os);
            return os.toByteArray();

        } catch( IOException e) {
            // can't happen.
            return new byte[0];
        }
    }

    /**
     * Decodes a Base64 string.
     * @param s  a Base64 String to be decoded.
     * @return   A String containing the decoded data.
     * @throws   IllegalArgumentException if the input is not valid Base64 encoded data.
     */
    public static String decode (String s)
    {
        return new String(decode(s.getBytes()));
    }

    /**
     * Decodes Base64 data.
     * No blanks or line breaks are allowed within the Base64 encoded data.
     * @param in  a byte array containing the Base64 encoded data.
     * @return    An array containing the decoded data bytes.
     * @throws    IllegalArgumentException if the input is not valid Base64 encoded data.
     */
    public static byte[] decode (byte[] in)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
        try {
            Base64Encoder encoder = new Base64Encoder();
            encoder.decode(in, 0, in.length, os);
            return os.toByteArray();
        } catch( IOException e) {
            // can't happen.
            return new byte[0];
        }

    }
}