package com.rockthevote.grommet.util;

import android.text.Editable;
import android.text.TextWatcher;

public class ZipTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().contains("-") && s.length() > 5) {
            s.insert(5, "-");
        } else if( s.toString().contains("-") && s.length() < 7){
            int dashIndex = s.toString().indexOf("-");
            s.delete(dashIndex, s.length());
        }
    }
}
