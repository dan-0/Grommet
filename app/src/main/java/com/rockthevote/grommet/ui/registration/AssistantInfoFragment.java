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

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.NameView;
import com.rockthevote.grommet.util.PhoneOrEmpty;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.Db.DEBOUNCE;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.ASSISTANT_PHONE;

public class AssistantInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.assistant_fields) View assistantFields;

    @BindView(R.id.assistant_name) NameView nameView;

    @BindView(R.id.assistant_address) AddressView addressView;

    @PhoneOrEmpty(messageResId = R.string.phone_format_error)
    @BindView(R.id.til_assistant_phone) TextInputLayout phoneTIL;

    @BindView(R.id.assistant_phone) EditText phoneEditText;

    @BindView(R.id.checkbox_has_assistant) CheckBox hasAssistant;

    @Checked
    @BindView(R.id.checkbox_assistant_affirmation) CheckBox assistantAffirmation;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private ObservableValidator validator;

    private PhoneNumberFormattingTextWatcher phoneFormatter;

    private CompositeSubscription subscriptions = new CompositeSubscription();

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

        Validator.registerAnnotation(PhoneOrEmpty.class);
        validator = new ObservableValidator(this, getActivity());

    }

    @Override
    public void onResume() {
        super.onResume();
        phoneFormatter = new PhoneNumberFormattingTextWatcher();
        phoneEditText.addTextChangedListener(phoneFormatter);

        subscriptions.add(RxTextView.afterTextChangeEvents(phoneEditText)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(event -> {
                    ContactMethod.insertOrUpdate(db, rockyRequestRowId.get(), ASSISTANT_PHONE,
                            new ContactMethod.Builder()
                                    .value(event.editable().toString())
                                    .build()
                    );
                }));

    }

    @Override
    public void onPause() {
        super.onPause();
        phoneEditText.removeTextChangedListener(phoneFormatter);
        subscriptions.unsubscribe();
    }

    @OnCheckedChanged(R.id.checkbox_has_assistant)
    public void onHasAssistantChecked(boolean checked) {
        assistantFields.setVisibility(checked ? View.VISIBLE : View.GONE);

        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .hasAssistant(checked)
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));

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
