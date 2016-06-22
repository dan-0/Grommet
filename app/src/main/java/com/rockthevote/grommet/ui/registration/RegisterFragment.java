package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.ui.misc.Truss;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class RegisterFragment extends BaseRegistrationFragment {

    @BindView(R.id.signature_pad) SignaturePad signaturePad;
    @BindView(R.id.textview_tos) TextView textViewToS;
    @BindView(R.id.button_register) Button buttonRegister;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_register);
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, v);

        CharSequence tos = new Truss()
                .append(getString(R.string.label_tos_prefix))
                .pushSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        //TODO remove this
                        Toast.makeText(getActivity(),"Terms of Service",Toast.LENGTH_SHORT).show();
                    }
                })
                .append(getString(R.string.label_tos_link))
                .popSpan()
                .build();

        textViewToS.setText(tos);
        textViewToS.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    @OnClick(R.id.clear_signature)
    public void onClearSignatureClick(View v){
        signaturePad.clear();
    }

    @OnCheckedChanged(R.id.checkbox_tos)
    public void onCheckChanged(boolean checked){
        buttonRegister.setEnabled(checked);
    }

}
