package com.rockthevote.grommet.util;

import com.mobsandgeeks.saripaar.AnnotationRule;

import commons.validator.routines.EmailValidator;

public class EmailOrEmptyRule extends AnnotationRule<EmailOrEmpty, String> {

    protected EmailOrEmptyRule(final EmailOrEmpty email) {
        super(email);
    }

    @Override
    public boolean isValid(final String email) {
        if (Strings.isBlank(email)) {
            return true;
        }
        return EmailValidator.getInstance(false).isValid(email);
    }
}