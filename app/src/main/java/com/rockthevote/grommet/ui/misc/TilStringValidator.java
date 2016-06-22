package com.rockthevote.grommet.ui.misc;

import android.support.design.widget.TextInputLayout;

import com.mobsandgeeks.saripaar.adapter.ViewDataAdapter;
import com.mobsandgeeks.saripaar.exception.ConversionException;

public class TilStringValidator implements ViewDataAdapter<TextInputLayout, String> {
    @Override
    public String getData(TextInputLayout view) throws ConversionException {
        if(view.getEditText() != null) {
            return view.getEditText().getText().toString();
        } else {
            return "";
        }
    }
}
