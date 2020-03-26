package com.rockthevote.grommet.ui.registration.personal;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.f2prateek.rx.preferences2.Preference;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.rockthevote.grommet.data.db.model.PhoneType;
import com.rockthevote.grommet.data.db.model.PreferredLanguage;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.data.prefs.PartnerVolunteerTextPref;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.util.EmailOrEmpty;
import com.rockthevote.grommet.util.Phone;
import com.rockthevote.grommet.util.Strings;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import com.rockthevote.grommet.data.db.model.Party;
import static com.rockthevote.grommet.data.db.model.Party.OTHER_PARTY;

import com.rockthevote.grommet.data.db.model.Race;

public class AdditionalInfoFragment extends BaseRegistrationFragment {
    private static final String OTHER_PARTY_VISIBILITY_KEY = "other_party_visibility_key";

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;
    @Inject @PartnerName Preference<String> partnerNamePref;
    @Inject @PartnerVolunteerTextPref Preference<PartnerVolunteerText> partnerVolunteerText;
    @BindView(R.id.spinner_race) BetterSpinner raceSpinner;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.spinner_party) BetterSpinner partySpinner;
    @BindView(R.id.political_party_change_textbox) CheckBox partyChangeCheckbox;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_other_party) TextInputLayout otherPartyTIL;
    @BindView(R.id.other_party_edit_text) EditText otherPartyEditText;

    @BindView(R.id.spinner_preferred_language) BetterSpinner langPrefSpinner;

    @Length(min = 8, max = 8, messageResId = R.string.error_penn_dot)
    @BindView(R.id.til_penn_dot) TextInputLayout pennDOTTIL;

    @BindView(R.id.penn_dot_edit_text) EditText pennDOTEditText;

    @Length(min = 4, max = 4, messageResId = R.string.error_ssn)
    @BindView(R.id.til_ssn_last_four) TextInputLayout ssnTIL;

    @BindView(R.id.ssn_last_four_edit_text) EditText ssnEditText;

    @EmailOrEmpty(messageResId = R.string.email_error)
    @BindView(R.id.til_email) TextInputLayout textInputEmail;

    @BindView(R.id.email_edit_text) EditText email;
    @BindView(R.id.email_opt_in) CheckBox emailOptIn;

    @Phone(messageResId = R.string.phone_format_error, allowEmpty = true)
    @BindView(R.id.til_phone_number) TextInputLayout phoneNumber;

    @BindView(R.id.phone) EditText phone;
    @BindView(R.id.spinner_phone_type) BetterSpinner phoneTypeSpinner;
    @BindView(R.id.checkbox_can_receive_text) CheckBox phoneOptIn;
    @BindView(R.id.checkbox_partner_volunteer_opt_in) CheckBox partnerVolunteerCheckBox;

    private ObservableValidator validator;
    private CompositeSubscription subscriptions;
    private EnumAdapter<Race> raceEnumAdapter;
    private EnumAdapter<Party> partyEnumAdapter;

    private EnumAdapter<PreferredLanguage> preferredLanguageEnumAdapter;
    private EnumAdapter<PhoneType> phoneTypeEnumAdapter;
    private PhoneNumberFormattingTextWatcher phoneFormatter;

    // must be initialized to true to trigger the observable default, unchecked, state (it's reversed)
    private final BehaviorSubject<Boolean> doesNotHavePennDOT = BehaviorSubject.create(false);
    private final BehaviorSubject<Boolean> doesNotHaveSSN = BehaviorSubject.create(false);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_additional_info);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Validator.registerAnnotation(Phone.class);
        Validator.registerAnnotation(EmailOrEmpty.class);
        Validator.registerAnnotation(NotEmpty.class);

        validator = new ObservableValidator(this, getActivity());

        // Setup Race Spinner
        raceEnumAdapter = new EnumAdapter<>(getActivity(), Race.class);
        raceSpinner.setAdapter(raceEnumAdapter);
        raceSpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            raceSpinner.getEditText().setText(raceEnumAdapter.getItem(i).toString());
            raceSpinner.dismiss();
        });


        // Setup Party Spinner
        partyEnumAdapter = new EnumAdapter<>(getActivity(), Party.class);
        partySpinner.setAdapter(partyEnumAdapter);
        partySpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            Party party = partyEnumAdapter.getItem(i);

            partySpinner.getEditText().setText(party.toString());
            partySpinner.dismiss();

            otherPartyTIL.setEnabled(OTHER_PARTY == party);
            otherPartyTIL.setVisibility(OTHER_PARTY == party ? View.VISIBLE : View.GONE);

            // clear the error out for a new attempt
            if (OTHER_PARTY == party) {
                otherPartyTIL.setErrorEnabled(false);
            }
        });


        // Setup Phone Type Spinner
        phoneTypeEnumAdapter = new EnumAdapter<>(getActivity(), PhoneType.class);
        phoneTypeSpinner.setAdapter(phoneTypeEnumAdapter);
        phoneTypeSpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            phoneTypeSpinner.getEditText().setText(phoneTypeEnumAdapter.getItem(i).toString());
            phoneTypeSpinner.dismiss();
        });

        if (null != savedInstanceState) {
            otherPartyTIL.setVisibility(savedInstanceState.getBoolean(OTHER_PARTY_VISIBILITY_KEY) ?
                    View.VISIBLE : View.GONE);
        }

        // set up preferred language spinner
        preferredLanguageEnumAdapter = new EnumAdapter<>(getActivity(), PreferredLanguage.class);
        langPrefSpinner.setAdapter(preferredLanguageEnumAdapter);
        langPrefSpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            langPrefSpinner.getEditText().setText(preferredLanguageEnumAdapter.getItem(i).toString());
            langPrefSpinner.dismiss();
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);

        // set up defaults
        if (null == savedInstanceState) {
            phoneTypeSpinner.getEditText().setText(phoneTypeEnumAdapter.getItem(0).toString());
        }

        phoneOptIn.setText(getString(R.string.label_receive_text, partnerNamePref.get()));
        emailOptIn.setText(getString(R.string.label_receive_email, partnerNamePref.get()));

        // update partner volunteer checkbox text
        Locale curLocale = getResources().getConfiguration().locale;
        switch (curLocale.getLanguage()) {
            case "es":
                partnerVolunteerCheckBox.setText(partnerVolunteerText.get().spanish());
                break;
            default: // use english for default
                partnerVolunteerCheckBox.setText(partnerVolunteerText.get().english());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        subscriptions = new CompositeSubscription();

        phoneFormatter = new PhoneNumberFormattingTextWatcher("US");
        phone.addTextChangedListener(phoneFormatter);

    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
        phone.removeTextChangedListener(phoneFormatter);
    }

    /**
     * listen for this here so the db update can happen on a separate thread
     *
     * @param checked
     */
    @OnCheckedChanged(R.id.does_not_have_penn_dot_checkbox)
    public void onDriversLicenseChecked(boolean checked) {
        pennDOTTIL.setVisibility(!checked ? View.VISIBLE : View.GONE);

        // disabling it prevents Saripaar from trying to validate it
        pennDOTTIL.setEnabled(!checked);
        if (checked) {
            pennDOTTIL.setErrorEnabled(false);
        }
        doesNotHavePennDOT.onNext(checked);
    }

    @OnCheckedChanged(R.id.ssn_last_four_checkbox)
    public void onSSNChecked(boolean checked) {
        ssnTIL.setVisibility(!checked ? View.VISIBLE : View.GONE);

        // disabling it prevents Saripaar from trying to validate it
        ssnTIL.setEnabled(!checked);
        if (checked) {
            ssnTIL.setErrorEnabled(false);
        }
        doesNotHaveSSN.onNext(checked);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(OTHER_PARTY_VISIBILITY_KEY, View.VISIBLE == otherPartyTIL.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Observable<Boolean> verify() {
        Observable<Boolean> emailOptInVerification = Observable.just(
                !emailOptIn.isChecked() || !Strings.isBlank(textInputEmail.getEditText().getText()));

        Observable<Boolean> phoneOptInVerification = Observable.just(
                !phoneOptIn.isChecked() || !Strings.isBlank(phoneNumber.getEditText().getText()));

        return Observable.zip(emailOptInVerification, phoneOptInVerification, validator.validate(),
                (emailRes, phoneRes, validatorRes) -> {
                    if (!emailRes) {
                        textInputEmail.setError(getString(R.string.email_error));
                    }

                    if (!phoneRes) {
                        phoneNumber.setError(getString(R.string.phone_format_error));
                    }

                    return emailRes && phoneRes && validatorRes;
                });
    }

    @Override
    public void storeState() {

    }
}
