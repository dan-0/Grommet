package com.rockthevote.grommet.ui.registration;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.rockthevote.grommet.R;

public class DatePickerDialogFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;

    public static DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener listener){
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setListener(listener);
        return fragment;
    }

    private void setListener(DatePickerDialog.OnDateSetListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // start the date picker at Jan 1, 1998
        return new DatePickerDialog(
                getActivity(),
                R.style.GrommetDatePickerDialog,
                listener,
                1998, 0, 1);
    }
}
