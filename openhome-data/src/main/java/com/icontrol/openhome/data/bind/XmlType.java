package com.icontrol.openhome.data.bind;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE) @Target({TYPE})
public @interface XmlType {

    String name() default "##default" ;

    String[] propOrder() default {""};

    String namespace() default "##default" ;

    @SuppressWarnings("rawtypes")
	Class factoryClass() default DEFAULT.class;

    static final class DEFAULT {}

    String factoryMethod() default "";
}
