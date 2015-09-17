package com.icontrol.android.openhomesimulator.util;

public class Hex {
    public static byte byteVal(byte hi, byte lo) {
        int val = 0;

        if (hi >= '0' && hi <= '9') {
            val = (hi - '0') << 4;
        } else if (hi >= 'A' && hi <= 'F') {
            val = (hi - 'A' + 10) << 4;
        } else {
            val = (hi - 'a' + 10) << 4;
        }
        if (lo >= '0' && lo <= '9') {
            val += (lo - '0');
        } else if (lo >= 'A' && lo <= 'F') {
            val += (lo - 'A' + 10);
        } else {
            val += (lo - 'a' + 10);
        }
        return (byte) val;
    }

    public static byte[] hexToBytes(String hexStr) {
        int numBytes = hexStr.length() / 2;
        byte[] bytes = new byte[numBytes];
        byte[] hexBytes = hexStr.getBytes();

        for (int i = 0; i < numBytes; i++) {
            bytes[i] = Hex.byteVal(hexBytes[i * 2], hexBytes[i * 2 + 1]);
        }
        hexBytes = null;
        return bytes;
    }

    public static String bytesToHex(byte[] value) {
        BytesToHex bth = new BytesToHex(value, 0, 0, value.length, 0);

        return bth.hexStr();
    }

    public static String bytesToHex(byte[] value, int start, int snib, int end, int enib) {
        BytesToHex bth = new BytesToHex(value, start, snib, end, enib);

        return bth.hexStr();
    }

    public static String bytesToHex(byte[] value, int start, int snib, int end, int enib, char delim) {
        BytesToHex bth = new BytesToHex(value, start, snib, end, enib);

        return bth.hexStr(delim);
    }

    private static char hex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static class BytesToHex {

        /**
         * Constructor.  Takes a byte array, plus an offset to the first byte and
         * nibble to be rendered as hex, and one past the last byte and nibble to
         * be rendered.  The leftmost nibble is designate nibble zero, the rightmost
         * nibble is nibble one.  For example, to render the first five nibbles of
         * an array (bytes 0-1 and the left half of byte 2), you'd pass in
         * 0, 0, 2, 1 as the parameters, since nibble 1 of byte 2 is one past what
         * you want to render.
         */
        private BytesToHex(byte[] value, int start, int snib, int end, int enib) {
            //TODO:  This method is declared 6 times throughout our code.  It should be declared once!
            this.nibble = snib;
            this.cur = start;
            this.end = end;
            this.enib = enib;
            this.value = new byte[value.length];
            System.arraycopy(value, 0, this.value, 0, value.length);
            this.str = new StringBuffer();
        }

        private boolean nextChar() {
            if (nibble == 0) {
                str.append(hex[(value[cur] >> 4) & 0x0f]);
            } else {
                str.append(hex[value[cur] & 0x0f]);
            }
            return next();
        }

        private boolean nextChar(char delim) {
            boolean end;

            if (nibble == 0) {
                str.append(hex[(value[cur] >> 4) & 0x0f]);
            } else {
                str.append(hex[value[cur] & 0x0f]);
            }
            end = !next();
            if (nibble == 0 && !end) {
                str.append(delim);
            }
            return !end;
        }

        private boolean next() {
            nibble = (nibble == 1) ? 0 : 1;
            if (nibble == 0) {
                cur++;
            }
            return !(cur == end && nibble == enib);
        }

        private int nibble;
        private int cur;
        private int end;
        private int enib;
        private byte[] value;
        private StringBuffer str;

        public String hexStr() {
            while (nextChar()) {
                ;
            }
            return str.toString();
        }

        public String hexStr(char delim) {
            while (nextChar(delim)) {
                ;
            }
            return str.toString();
        }
    }
}
