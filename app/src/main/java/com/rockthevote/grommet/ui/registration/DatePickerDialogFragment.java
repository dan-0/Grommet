package com.rockthevote.grommet.ui.registration;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.rockthevote.grommet.R;

import java.util.Calendar;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(),
                R.style.GrommetDatePickerDialog,
                listener,
                year, month, day);
        return dialog;
    }
}
