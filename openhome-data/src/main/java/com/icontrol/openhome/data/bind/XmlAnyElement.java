package com.icontrol.openhome.data.bind;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(SOURCE)
@Target({FIELD,METHOD})
public @interface XmlAnyElement {

    boolean lax() default false;

    Class<?> value() default DEFAULT.class;
    
    static final class DEFAULT {}
}
