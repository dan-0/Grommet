package com.rockthevote.grommet.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.mobsandgeeks.saripaar.AnnotationRule;

public class PhoneOrEmptyRule extends AnnotationRule<PhoneOrEmpty, String> {

    protected PhoneOrEmptyRule(final PhoneOrEmpty email) {
        super(email);
    }

    @Override
    public boolean isValid(final String phone) {
        if (Strings.isBlank(phone)) {
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