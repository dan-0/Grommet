package com.rockthevote.grommet.ui.registration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.data.prefs.RegistrationText;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationCompleteDialogFragment extends DialogFragment {

    @Inject @RegistrationText
    Preference<RegistrationNotificationText> registrationTextPref;

    @BindView(R.id.drc_text_view) TextView content;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Injector.obtain(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);

        View v = inflater.inflate(R.layout.dialog_registration_complete, container);
        ButterKnife.bind(this, v);

        getDialog().setTitle(getString(R.string.registration_complete));
        RegistrationNotificationText regText = registrationTextPref.get();

        Locale curLocale = getResources().getConfiguration().locale;
        switch (curLocale.getLanguage()) {
            case "es":
                content.setText(regText.spanish());
                break;
            default: // use english for default
                content.setText(regText.english());
                break;
        }
        return v;
    }

    @OnClick(R.id.drc_ok_button)
    public void onOKClick(View v) {
        getActivity().finish();
    }
}
