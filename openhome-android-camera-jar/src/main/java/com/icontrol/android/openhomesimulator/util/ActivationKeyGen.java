package com.icontrol.android.openhomesimulator.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.security.MessageDigest;

public class ActivationKeyGen {

    private static final Log log = LogFactory.getLog(ActivationKeyGen.class);

    /**
     * Useful method for converting a hex number string into its byte representation
     *
     * @param numberstr
     * @param bytes
     */
    public static void readIntBytes(String numberstr, byte[] bytes) {
        String numStrVal = numberstr;
        if (numberstr.length() / 2 != bytes.length) {
            if (numberstr.length() / 2 > bytes.length) {
                log.warn("readIntBytes too long truncating! [" + numberstr + "] " + bytes.length);
                numStrVal = numberstr.substring(numberstr.length() / 2 - bytes.length, numberstr.length());
            } else {
                log.warn("readIntBytes too short, appending with 0 [" + numberstr + "] " + bytes.length);
                while (numberstr.length() / 2 != bytes.length) {
                    numStrVal = "0" + numberstr;
                }
            }
        }
        for (int i = 0; i < numStrVal.length() / 2; i++) {
            bytes[i] = (byte) Integer.parseInt(numStrVal.substring(2 * i, 2 * i + 2), 16);
        }
    }

    /**
     * Generate activation key from serial number using new 3.4 method.
     *
     * @param serial in the format 006035000004
     * @return activation key (uppercase) or throws exception
     */
    public static String generateActivationKey(String serial, String key_code) throws IOException {
        String serialVal = serial.toLowerCase();
        // check activation key using the new 3.4 algorithm
        if (key_code != null) {
            try {
                // get the md5( keyGlobalCode + serial )
                String seed = key_code + serialVal;
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] cleartext = seed.getBytes();

                md.update(cleartext);
                byte[] dbytes = md.digest();

                String md5check = Hex.bytesToHex(dbytes, 0, 0, dbytes.length, 0);
                String key = serialVal.substring(2) + md5check.substring(0, 8);
                // tracer.trace("generateActivationKey "+serialVal+" key:"+key);
                byte[] prefixbytes = new byte[9];
                readIntBytes(key, prefixbytes);
                int crc = CRC8.compute(prefixbytes);
                byte[] crcbytes = new byte[1];
                crcbytes[0] = (byte) crc;
                String crccheck = Hex.bytesToHex(crcbytes, 0, 0, crcbytes.length, 0);
                key += crccheck;
                return key.toUpperCase();
            } catch (Exception e) {
                log.error("generateActivationKey " + serialVal + " exception e:" + e);
                throw new IOException("exception generating key:" + e);
            }
        } else {
            throw new IOException("server not configured with Vendor KeyCode" );
        }
    }

}