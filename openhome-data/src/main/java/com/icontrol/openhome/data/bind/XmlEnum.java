package com.icontrol.openhome.data.bind;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE) @Target({TYPE})
public @interface XmlEnum {

    Class<?> value() default String.class;
}
