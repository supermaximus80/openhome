package com.icontrol.openhome.data.bind;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE) @Target({FIELD, METHOD, PARAMETER})
public @interface XmlElement {
	

    String name() default "##default";

    boolean nillable() default false;

    boolean required() default false;


    String namespace() default "##default";


    String defaultValue() default "\u0000";

    @SuppressWarnings("rawtypes")
	Class type() default DEFAULT.class;

    static final class DEFAULT {}

}
