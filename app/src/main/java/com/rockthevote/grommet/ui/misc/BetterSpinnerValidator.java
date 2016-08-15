package com.rockthevote.grommet.ui.misc;

import com.mobsandgeeks.saripaar.adapter.ViewDataAdapter;
import com.mobsandgeeks.saripaar.exception.ConversionException;

public class BetterSpinnerValidator implements ViewDataAdapter<BetterSpinner, String> {
    @Override
    public String getData(BetterSpinner view) throws ConversionException {
        if (view.getEditText() != null) {
            return view.getEditText().getText().toString();
        } else {
            return "";
        }
    }
}
