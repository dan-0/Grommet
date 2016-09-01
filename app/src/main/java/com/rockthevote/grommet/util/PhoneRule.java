package com.rockthevote.grommet.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.mobsandgeeks.saripaar.AnnotationRule;

public class PhoneRule extends AnnotationRule<Phone, String> {

    protected PhoneRule(final Phone phone) {
        super(phone);
    }

    @Override
    public boolean isValid(final String phone) {
        if (mRuleAnnotation.allowEmpty() && Strings.isBlank(phone)) {
            return true;
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber number;
        try {
            number = phoneUtil.parse(phone, "US");
        } catch (NumberParseException e) {
            return false;
        }
        return PhoneNumberUtil.getInstance().isValidNumber(number);
    }

}