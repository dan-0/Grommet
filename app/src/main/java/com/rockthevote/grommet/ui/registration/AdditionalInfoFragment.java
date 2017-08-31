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
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterId;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.util.EmailOrEmpty;
import com.rockthevote.grommet.util.Phone;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.Db.DEBOUNCE;
import static com.rockthevote.grommet.data.db.model.AdditionalInfo.Type.LANGUAGE_PREF;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.EMAIL;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.PHONE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Party;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Party.OTHER_PARTY;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Race;
import static com.rockthevote.grommet.data.db.model.VoterId.Type.DRIVERS_LICENSE;
import static com.rockthevote.grommet.data.db.model.VoterId.Type.SSN_LAST_FOUR;

public class AdditionalInfoFragment extends BaseRegistrationFragment {
    private static final String OTHER_PARTY_VISIBILITY_KEY = "other_party_visibility_key";

    @BindView(R.id.spinner_race) BetterSpinner raceSpinner;

    @NotEmpty
    @BindView(R.id.spinner_party) BetterSpinner partySpinner;

    @NotEmpty
    @BindView(R.id.til_other_party) TextInputLayout otherPartyTIL;
    @BindView(R.id.other_party_edit_text) EditText otherPartyEditText;

    @BindView(R.id.preferred_language) EditText preferredLanguage;

    @BindView(R.id.does_not_have_penn_dot_checkbox) CheckBox noPennDOTCheckbox;

    @Length(min = 8, max = 8, messageResId = R.string.error_penn_dot)
    @BindView(R.id.til_penn_dot) TextInputLayout pennDOTTIL;

    @BindView(R.id.penn_dot_edit_text) EditText pennDOTEditText;

    @BindView(R.id.ssn_last_four_checkbox) CheckBox noSSNCheckBox;
    
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

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject @PartnerName Preference<String> partnerNamePref;

    @Inject BriteDatabase db;

    private ObservableValidator validator;
    private CompositeSubscription subscriptions;
    private EnumAdapter<Race> raceEnumAdapter;
    private EnumAdapter<Party> partyEnumAdapter;

    private EnumAdapter<RockyRequest.PhoneType> phoneTypeEnumAdapter;
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
        phoneTypeEnumAdapter = new EnumAdapter<>(getActivity(), RockyRequest.PhoneType.class);
        phoneTypeSpinner.setAdapter(phoneTypeEnumAdapter);
        phoneTypeSpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            phoneTypeSpinner.getEditText().setText(phoneTypeEnumAdapter.getItem(i).toString());
            phoneTypeSpinner.dismiss();
        });

        if (null != savedInstanceState) {
            otherPartyTIL.setVisibility(savedInstanceState.getBoolean(OTHER_PARTY_VISIBILITY_KEY) ?
                    View.VISIBLE : View.GONE);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set up defaults
        if (null == savedInstanceState) {
            raceSpinner.getEditText().setText(Race.OTHER.toString());
            phoneTypeSpinner.getEditText().setText(phoneTypeEnumAdapter.getItem(0).toString());
        }

        phoneOptIn.setText(getString(R.string.label_receive_text, partnerNamePref.get()));
        emailOptIn.setText(getString(R.string.label_receive_email, partnerNamePref.get()));
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        phoneFormatter = new PhoneNumberFormattingTextWatcher("US");
        phone.addTextChangedListener(phoneFormatter);

        subscriptions.add(RxTextView.afterTextChangeEvents(raceSpinner.getEditText())
                .observeOn(Schedulers.io())
                .skip(1)
                .subscribe(race -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .race(Race.fromString(race.editable().toString()))
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

        subscriptions.add(RxTextView.afterTextChangeEvents(otherPartyEditText)
                .observeOn(Schedulers.io())
                .skip(1)
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(otherParty -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .otherParty(otherParty.editable().toString())
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

        subscriptions.add(RxTextView.afterTextChangeEvents(partySpinner.getEditText())
                .observeOn(Schedulers.io())
                .skip(1)
                .subscribe(party -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .party(Party.fromString(party.editable().toString()))
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

        subscriptions.add(RxTextView.afterTextChangeEvents(preferredLanguage)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(event -> {
                    AdditionalInfo.insertOrUpdate(db, rockyRequestRowId.get(),
                            LANGUAGE_PREF, new AdditionalInfo.Builder()
                                    .type(LANGUAGE_PREF)
                                    .stringValue(event.editable().toString())
                                    .build());
                }));

        subscriptions.add(Observable.combineLatest(RxTextView.afterTextChangeEvents(pennDOTEditText),
                doesNotHavePennDOT,
                (driversLicense, doesNotHave) -> new VoterId.Builder()
                        .type(DRIVERS_LICENSE)
                        .value(doesNotHave ? "" : driversLicense.editable().toString())
                        .attestNoSuchId(doesNotHave)
                        .build())
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(contentValues -> {
                    VoterId.insertOrUpdate(db, rockyRequestRowId.get(), DRIVERS_LICENSE, contentValues);
                }));

        subscriptions.add(Observable.combineLatest(RxTextView.afterTextChangeEvents(ssnEditText),
                doesNotHaveSSN,
                (ssn, doesNotHave) -> new VoterId.Builder()
                        .type(SSN_LAST_FOUR)
                        .value(doesNotHave ? "" : ssn.editable().toString())
                        .attestNoSuchId(doesNotHave)
                        .build())
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(contentValues -> {
                    VoterId.insertOrUpdate(db, rockyRequestRowId.get(), SSN_LAST_FOUR, contentValues);
                }));

        subscriptions.add(RxTextView.afterTextChangeEvents(email)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(event ->
                {
                    ContactMethod.insertOrUpdate(db, rockyRequestRowId.get(), EMAIL,
                            new ContactMethod.Builder()
                                    .value(event.editable().toString())
                                    .build());
                }));

        subscriptions.add(RxCompoundButton.checkedChanges(emailOptIn)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(checked -> {
                    db.update(
                            RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .partnerOptInEmail(checked)
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

        subscriptions.add(RxTextView.afterTextChangeEvents(phone)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(event -> {
                    ContactMethod.insertOrUpdate(db, rockyRequestRowId.get(), PHONE,
                            new ContactMethod.Builder()
                                    .value(event.editable().toString())
                                    .build()
                    );
                }));

        subscriptions.add(RxTextView.afterTextChangeEvents(phoneTypeSpinner.getEditText())
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(phoneType -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .phoneType(RockyRequest.PhoneType.fromString(phoneType.editable().toString()))
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));

                }));

        subscriptions.add(RxCompoundButton.checkedChanges(phoneOptIn)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(checked -> {
                    db.update(
                            RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .partnerOptInSMS(checked)
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

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
                new Func3<Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean emailRes, Boolean phoneRes, Boolean validatorRes) {
                        if(!emailRes){
                            textInputEmail.setError(getString(R.string.email_error));
                        }

                        if(!phoneRes){
                            phoneNumber.setError(getString(R.string.phone_format_error));
                        }

                        return emailRes && phoneRes && validatorRes;
                    }
                });
    }
}
