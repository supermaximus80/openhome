package com.icontrol.openhome.data.bind;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(SOURCE)
public @interface XmlSeeAlso {
    @SuppressWarnings("rawtypes")
	Class[] value();
}
