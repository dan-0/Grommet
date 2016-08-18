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
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterId;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.util.EmailOrEmpty;
import com.rockthevote.grommet.util.PhoneOrEmpty;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.Db.DEBOUNCE;
import static com.rockthevote.grommet.data.db.model.AdditionalInfo.Type.LANGUAGE_PREF;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.EMAIL;
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.PHONE;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Party;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Race;
import static com.rockthevote.grommet.data.db.model.VoterId.Type.DRIVERS_LICENSE;
import static com.rockthevote.grommet.data.db.model.VoterId.Type.SSN_LAST_FOUR;

public class AdditionalInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.spinner_race) BetterSpinner raceSpinner;

    @BindView(R.id.spinner_party) BetterSpinner partySpinner;

    @BindView(R.id.preferred_language) EditText preferredLanguage;

    @BindView(R.id.til_drivers_license) TextInputLayout driversLicenseTIL;

    @BindView(R.id.drivers_license_checkbox) CheckBox hasDriversLicense;

    @NotEmpty
    @BindView(R.id.drivers_license) EditText driversLicenseEditText;

    @BindView(R.id.til_ssn_last_four) TextInputLayout ssnTIL;

    @BindView(R.id.ssn_last_four_checkbox) CheckBox hasSSNCheckBox;

    @NotEmpty
    @BindView(R.id.ssn_last_four_edit_text) EditText ssnEditText;

    @EmailOrEmpty(messageResId = R.string.email_error)
    @BindView(R.id.til_email) TextInputLayout textInputEmail;

    @BindView(R.id.email_edit_text) EditText email;

    @BindView(R.id.email_opt_in) CheckBox emailOptIn;

    @PhoneOrEmpty(messageResId = R.string.phone_format_error)
    @BindView(R.id.til_phone_number) TextInputLayout phoneNumber;

    @BindView(R.id.phone) EditText phone;

    @BindView(R.id.spinner_phone_type) BetterSpinner phoneTypeSpinner;

    @BindView(R.id.checkbox_can_receive_text) CheckBox phoneOptIn;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private ObservableValidator validator;
    private CompositeSubscription subscriptions;
    private EnumAdapter<Race> raceEnumAdapter;
    private EnumAdapter<Party> partyEnumAdapter;

    private EnumAdapter<RockyRequest.PhoneType> phoneTypeEnumAdapter;
    private PhoneNumberFormattingTextWatcher phoneFormatter;

    private final PublishSubject<Boolean> hasDriversLicenseChecked = PublishSubject.create();
    private final PublishSubject<Boolean> hasSSNChecked = PublishSubject.create();

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

        Validator.registerAnnotation(PhoneOrEmpty.class);
        Validator.registerAnnotation(EmailOrEmpty.class);

        validator = new ObservableValidator(this, getActivity());

        raceEnumAdapter = new EnumAdapter<>(getActivity(), Race.class);
        raceSpinner.setAdapter(raceEnumAdapter);
        raceSpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            raceSpinner.getEditText().setText(raceEnumAdapter.getItem(i).toString());
            raceSpinner.dismiss();
        });
        raceSpinner.getEditText().setText(Race.OTHER.toString());

        partyEnumAdapter = new EnumAdapter<>(getActivity(), Party.class);
        partySpinner.setAdapter(partyEnumAdapter);
        partySpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            partySpinner.getEditText().setText(partyEnumAdapter.getItem(i).toString());
            partySpinner.dismiss();
        });
        partySpinner.getEditText().setText(Party.OTHER.toString());

        phoneTypeEnumAdapter = new EnumAdapter<>(getActivity(), RockyRequest.PhoneType.class);
        phoneTypeSpinner.setAdapter(phoneTypeEnumAdapter);
        phoneTypeSpinner.setOnItemClickListener((adapterView, view1, i, l) -> {
            phoneTypeSpinner.getEditText().setText(phoneTypeEnumAdapter.getItem(i).toString());
            phoneTypeSpinner.dismiss();
        });
        phoneTypeSpinner.getEditText().setText(phoneTypeEnumAdapter.getItem(0).toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        phoneFormatter = new PhoneNumberFormattingTextWatcher();
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
                .skip(1)
                .subscribe(event -> {
                    AdditionalInfo.insertOrUpdate(db, rockyRequestRowId.get(),
                            LANGUAGE_PREF, new AdditionalInfo.Builder()
                                    .type(LANGUAGE_PREF)
                                    .stringValue(event.editable().toString())
                                    .build());
                }));

        subscriptions.add(Observable.combineLatest(RxTextView.afterTextChangeEvents(driversLicenseEditText),
                hasDriversLicenseChecked,
                (driversLicense, checkChange) -> new VoterId.Builder()
                        .type(DRIVERS_LICENSE)
                        .value(checkChange ? driversLicense.editable().toString() : "")
                        .attestNoSuchId(checkChange)
                        .build())
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(contentValues -> {
                    VoterId.insertOrUpdate(db, rockyRequestRowId.get(), DRIVERS_LICENSE, contentValues);
                }));

        subscriptions.add(Observable.combineLatest(RxTextView.afterTextChangeEvents(ssnEditText),
                hasSSNChecked,
                (ssn, hasSSN) -> new VoterId.Builder()
                        .type(SSN_LAST_FOUR)
                        .value(hasSSN ? ssn.editable().toString() : "")
                        .attestNoSuchId(hasSSN)
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
                                    .optInEmail(checked)
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
                                    .optInSMS(checked)
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
    @OnCheckedChanged(R.id.drivers_license_checkbox)
    public void onDriversLicenseChecked(boolean checked) {
        driversLicenseTIL.setVisibility(checked ? View.VISIBLE : View.GONE);

        // disabling it prevents Saripaar from trying to validate it
        driversLicenseTIL.setEnabled(checked);
        driversLicenseTIL.setErrorEnabled(checked);
        hasDriversLicenseChecked.onNext(checked);
    }

    @OnCheckedChanged(R.id.ssn_last_four_checkbox)
    public void onSSNChecked(boolean checked) {
        ssnTIL.setVisibility(checked ? View.VISIBLE : View.GONE);

        // disabling it prevents Saripaar from trying to validate it
        ssnTIL.setEnabled(checked);
        ssnTIL.setErrorEnabled(checked);
        hasSSNChecked.onNext(checked);
    }

    @Override
    public Observable<Boolean> verify() {
        return validator.validate();
    }
}
