package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.f2prateek.rx.preferences2.Preference;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.NameView;
import com.rockthevote.grommet.util.Phone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class AssistantInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.assistant_fields) View assistantFields;

    @BindView(R.id.assistant_name) NameView nameView;

    @BindView(R.id.assistant_address) AddressView addressView;

    @Phone(messageResId = R.string.phone_format_error)
    @BindView(R.id.til_assistant_phone) TextInputLayout phoneTIL;

    @BindView(R.id.assistant_phone) EditText phoneEditText;

    @BindView(R.id.checkbox_has_assistant) CheckBox hasAssistant;

    @Checked(messageResId = R.string.must_be_checked)
    @BindView(R.id.checkbox_assistant_affirmation) CheckBox assistantAffirmation;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    private ObservableValidator validator;

    private PhoneNumberFormattingTextWatcher phoneFormatter;

    private CompositeSubscription subscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_assistant_info);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Validator.registerAnnotation(Phone.class);
        validator = new ObservableValidator(this, getActivity());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        phoneFormatter = new PhoneNumberFormattingTextWatcher("US");
        phoneEditText.addTextChangedListener(phoneFormatter);


    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
        phoneEditText.removeTextChangedListener(phoneFormatter);
    }

    @OnCheckedChanged(R.id.checkbox_has_assistant)
    public void onHasAssistantChecked(boolean checked) {
        assistantFields.setVisibility(checked ? View.VISIBLE : View.GONE);

    }

    @Override
    public Observable<Boolean> verify() {

        if (!hasAssistant.isChecked()) {
            return super.verify();
        }

        return Observable.zip(nameView.verify(), addressView.verify(), validator.validate(),
                (name, address, other) -> name && address && other);
    }
}
