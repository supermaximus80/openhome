package com.icontrol.openhomesimulator.util;

import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

/*
 * Ported from Apache's HTTPClient
 * @author rbitonio
 */

/**
 * Some HTTP headers (such as the set-cookie header) have values that
 * can be decomposed into multiple elements.  Such headers must be in the
 * following form:
 * <p/>
 * <PRE>
 * header  = [ element ] *( "," [ element ] )
 * element = name [ "=" [ value ] ] *( ";" [ param ] )
 * param   = name [ "=" [ value ] ]
 * <p/>
 * name    = token
 * value   = ( token | quoted-string )
 * <p/>
 * token         = 1*<any char except "=", ",", ";", <"> and
 * white space>
 * quoted-string = <"> *( text | quoted-char ) <">
 * text          = any char except <">
 * quoted-char   = "\" char
 * </PRE>
 * <p/>
 * <P>   Any amount of white space is allowed between any part of the
 * header, element or param and is ignored. A missing value in any
 * element or param will be stored as the empty string; if the "="
 * is also missing <var>null</var> will be stored instead.
 * <p/>
 * <P>   This class represents an individual header element.  This
 * class also has a <CODE>parse()</CODE> method for parsing a header
 * value into an array of elements.
 */
public class HeaderElement extends NameValuePair {


    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public HeaderElement() {
        super();
    }


    /**
     * Constructor.
     */
    public HeaderElement(String name, String value) {
        super(name, value);
    }

    /**
     * Constructor.
     */
    public HeaderElement(String name, String value,
                         NameValuePair[] parameters) {
        super(name, value);
        this.parameters = parameters;
    }

    // -------------------------------------------------------- Class Variables

    private static final BitSet SEPARATORS = new BitSet(128);
    private static final BitSet TOKEN_CHAR = new BitSet(128);
    private static final BitSet UNSAFE_CHAR = new BitSet(128);

    static {
        // rfc-2068 tspecial
        SEPARATORS.set('(');
        SEPARATORS.set(')');
        SEPARATORS.set('<');
        SEPARATORS.set('>');
        SEPARATORS.set('@');
        SEPARATORS.set(',');
        SEPARATORS.set(';');
        SEPARATORS.set(':');
        SEPARATORS.set('\\');
        SEPARATORS.set('"');
        SEPARATORS.set('/');
        SEPARATORS.set('[');
        SEPARATORS.set(']');
        SEPARATORS.set('?');
        SEPARATORS.set('=');
        SEPARATORS.set('{');
        SEPARATORS.set('}');
        SEPARATORS.set(' ');
        SEPARATORS.set('\t');

        // rfc-2068 token
        for (int ch = 32; ch < 127; ch++) {
            TOKEN_CHAR.set(ch);
        }
        TOKEN_CHAR.xor(SEPARATORS);

        // rfc-1738 unsafe characters, including CTL and SP, and excluding
        // "#" and "%"
        for (int ch = 0; ch < 32; ch++) {
            UNSAFE_CHAR.set(ch);
        }
        UNSAFE_CHAR.set(' ');
        UNSAFE_CHAR.set('<');
        UNSAFE_CHAR.set('>');
        UNSAFE_CHAR.set('"');
        UNSAFE_CHAR.set('{');
        UNSAFE_CHAR.set('}');
        UNSAFE_CHAR.set('|');
        UNSAFE_CHAR.set('\\');
        UNSAFE_CHAR.set('^');
        UNSAFE_CHAR.set('~');
        UNSAFE_CHAR.set('[');
        UNSAFE_CHAR.set(']');
        UNSAFE_CHAR.set('`');
        UNSAFE_CHAR.set(127);
    }

    // ----------------------------------------------------- Instance Variables


    /**
     * Name.
     */
    protected NameValuePair[] parameters = null;

    // ------------------------------------------------------------- Properties

    public NameValuePair[] getParameters() {
        return this.parameters;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * This parses the value part of a header. The result is an array of
     * HeaderElement objects.
     *
     * @param headerValue the string representation of the header value
     *                    (as received from the web server).
     * @return the header elements containing <code>Header</code> elements.
     * @throws Exception if the above syntax rules are violated.
     */
    public final static HeaderElement[] parse(String headerValue)
            throws Exception {
        if (headerValue == null)
            return null;
        Vector elements = new Vector();

        StringTokenizer tokenizer =
                new StringTokenizer(headerValue.trim(), ",");

        while (tokenizer.countTokens() > 0) {
            String nextToken = tokenizer.nextToken();

            // careful... there may have been a comma in a quoted string
            try {
                while (HeaderElement.hasOddNumberOfQuotationMarks(
                        nextToken)) {
                    nextToken += "," + tokenizer.nextToken();
                }
            } catch (NoSuchElementException exception) {
                throw new Exception(
                        "Bad header format: wrong number of quotation marks");
            }

            try {
                /**
                 * Following to RFC 2109 and 2965, in order not to conflict
                 * with the next header element, make it sure to parse tokens.
                 * the expires date format is "Wdy, DD-Mon-YY HH:MM:SS GMT".
                 * Notice that there is always comma(',') sign.
                 * For the general cases, rfc1123-date, rfc850-date.
                 */
                if (tokenizer.hasMoreTokens()) {
                    if (nextToken.endsWith("Mon") ||
                            nextToken.endsWith("Tue") ||
                            nextToken.endsWith("Wed") ||
                            nextToken.endsWith("Thu") ||
                            nextToken.endsWith("Fri") ||
                            nextToken.endsWith("Sat") ||
                            nextToken.endsWith("Sun") ||
                            nextToken.endsWith("Monday") ||
                            nextToken.endsWith("Tuesday") ||
                            nextToken.endsWith("Wednesday") ||
                            nextToken.endsWith("Thursday") ||
                            nextToken.endsWith("Friday") ||
                            nextToken.endsWith("Saturday") ||
                            nextToken.endsWith("Sunday")) {

                        nextToken += tokenizer.nextToken(",");
                    }
                }
            } catch (NoSuchElementException exception) {
                throw new Exception
                        ("Bad header format: parsing with wrong header elements");
            }

            String tmp = nextToken.trim();
            if (!tmp.endsWith(";")) {
                tmp += ";";
            }
            char[] header = tmp.toCharArray();

            boolean inAString = false;
            int startPos = 0;
            HeaderElement element = new HeaderElement();
            Vector parameters = new Vector();
            for (int i = 0; i < header.length; i++) {
                if (header[i] == ';' && !inAString) {
                    NameValuePair pair = parsePair(header, startPos, i);
                    if (pair == null) {
                        throw new Exception(
                                "Bad header format: empty name/value pair in" +
                                        nextToken);

                        // the first name/value pair are handled differently
                    } else if (startPos == 0) {
                        element.setName(pair.getName());
                        element.setValue(pair.getValue());
                    } else {
                        parameters.addElement(pair);
                    }
                    startPos = i + 1;
                } else if (header[i] == '"' &&
                        !(inAString && i > 0 && header[i - 1] == '\\')) {
                    inAString = !inAString;
                }
            }

            // now let's add all the parameters into the header element
            if (parameters.size() > 0) {
                NameValuePair[] tmp2 = new NameValuePair[parameters.size()];
                parameters.copyInto((NameValuePair[]) tmp2);
                element.parameters = tmp2;
                parameters.removeAllElements();
            }

            // and save the header element into the list of header elements
            elements.addElement(element);
        }

        HeaderElement[] headerElements = new HeaderElement[elements.size()];
        elements.copyInto((HeaderElement[]) headerElements);
        return headerElements;
    }

    private final static boolean hasOddNumberOfQuotationMarks(String string) {
        boolean odd = false;
        int start = -1;
        while ((start = string.indexOf('"', start + 1)) != -1) {
            odd = !odd;
        }
        return odd;
    }

    private final static NameValuePair parsePair(
            char[] header, int start, int end)
            throws Exception {

        boolean done = false;
        NameValuePair pair = null;
        String name = new String(header, start, end - start).trim();
        String value = null;

        int index = name.indexOf("=");
        if (index >= 0) {
            if ((index + 1) < name.length()) {
                value = name.substring(index + 1).trim();

                // strip quotation marks
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                // is there anything left?
                if (value.length() == 0) {
                    value = null;
                }
            }
            name = name.substring(0, index).trim();
        }

        if (name != null && name.length() > 0) {
            pair = new NameValuePair(name, value);
        }

        return pair;
    }

    public static void main(String[] args) {
        // let's test this class
        try {
            String headerValue = "Digest realm=\"MemoryRealm\", qop=\"auth\", nonce=\"4a1ecf8a8838694cb2977bbad7fdf1fc\", opaque=\"378cd072d6eb97c5f2d4cc2060209648\"";

            //String headerValue = "name1 = value1; name2; name3=\"value3\" , name4=value4; " + "name5=value5, name6= ; name7 = value7; name8 = \" name8\"";
            HeaderElement[] elements = HeaderElement.parse(headerValue);
            for (int i = 0; i < elements.length; i++) {
                System.out.println("name =>" + elements[i].getName());
                System.out.println("value=>" + elements[i].getValue());
                if (elements[i].parameters != null) {
                    for (int j = 0; j < elements[i].parameters.length; j++) {
                        System.out.println("parameter name =>" + elements[i].parameters[j].getName());
                        System.out.println("parameter value=>" + elements[i].parameters[j].getValue());
                    }
                }
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }
}