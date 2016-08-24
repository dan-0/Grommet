package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationCompleteDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        View v = inflater.inflate(R.layout.dialog_registration_complete, container);
        ButterKnife.bind(this, v);
        getDialog().setTitle(getString(R.string.registration_complete));
        return v;
    }

    @OnClick(R.id.drc_ok_button)
    public void onOKClick(View v) {
        getActivity().finish();
    }
}
