package com.icontrol.openhome.data.bind;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({METHOD})
public @interface XmlElementDecl {

    @SuppressWarnings("rawtypes")
	Class scope() default GLOBAL.class;

    String namespace() default "##default";

    String name();

    String substitutionHeadNamespace() default "##default";

    String substitutionHeadName() default "";

    String defaultValue() default "\u0000";

    public final class GLOBAL {}
}

