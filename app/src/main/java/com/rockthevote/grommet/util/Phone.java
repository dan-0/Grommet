package com.rockthevote.grommet.util;


import com.mobsandgeeks.saripaar.annotation.ValidateUsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ValidateUsing(PhoneRule.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface Phone {
    int messageResId() default -1;                     // Mandatory attribute

    String message() default "Oops... too pricey";   // Mandatory attribute

    int sequence() default -1;                     // Mandatory attribute

    boolean allowEmpty() default false;
}
