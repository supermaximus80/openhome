package com.icontrol.openhome.data.bind;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.icontrol.openhome.data.bind.XmlElement;

@Retention(SOURCE) @Target({FIELD,METHOD})
public @interface XmlElements {

    XmlElement[] value();
}
