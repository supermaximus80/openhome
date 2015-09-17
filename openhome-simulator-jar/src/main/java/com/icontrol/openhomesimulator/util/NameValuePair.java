package com.icontrol.openhomesimulator.util;

import java.io.Serializable;

/*
 * Ported from Apache's HTTPClient
 * @author rbitonio
 */

public class NameValuePair implements Serializable {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public NameValuePair() {
    }

    /**
     * Constructor.
     */
    public NameValuePair(String name, String value) {

        this.name = name;
        this.value = value;

    }

    // ----------------------------------------------------- Instance Variables

    /**
     * Name.
     */
    protected String name = null;

    /**
     * Value.
     */
    protected String value = null;


    // ------------------------------------------------------------- Properties

    /**
     * Name property setter.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Name property getter.
     *
     * @return String name
     */
    public String getName() {
        return name;
    }


    /**
     * Value property setter.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }


    /**
     * Value property getter.
     *
     * @return String value
     */
    public String getValue() {
        return value;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Get a String representation of the header.
     */
    public String toString() {
        return ("name=" + name + ", " + "value=" + value);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (this.getClass().equals(object.getClass())) {
            NameValuePair pair = (NameValuePair) object;
            return (this.name.equals(pair.name) &&
                    this.value.equals(pair.value));
        } else {
            return false;
        }
    }
}