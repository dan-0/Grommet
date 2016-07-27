package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.AdditionalInfo;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.VoterId;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
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
import static com.rockthevote.grommet.data.db.model.RockyRequest.Party;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Race;
import static com.rockthevote.grommet.data.db.model.VoterId.Type.DRIVERS_LICENSE;

public class AdditionalInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.spinner_race) Spinner raceSpinner;
    @BindView(R.id.spinner_party) Spinner partySpinner;
    @BindView(R.id.preferred_language) EditText preferredLanguage;
    @BindView(R.id.til_drivers_license) TextInputLayout driversLicenseTIL;

    @BindView(R.id.drivers_license_checkbox) CheckBox hasDriversLicense;

    @NotEmpty
    @BindView(R.id.drivers_license) EditText driversLicenseEditText;


    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private ObservableValidator validator;
    private CompositeSubscription subscriptions;
    private EnumAdapter<Race> raceEnumAdapter;
    private EnumAdapter<Party> partyEnumAdapter;

    private final PublishSubject<Boolean> hasDriversLicenseChecked = PublishSubject.create();

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
        validator = new ObservableValidator(this, getActivity());

        raceEnumAdapter = new EnumAdapter<>(getActivity(), Race.class);
        raceSpinner.setAdapter(raceEnumAdapter);

        partyEnumAdapter = new EnumAdapter<>(getActivity(), Party.class);
        partySpinner.setAdapter(partyEnumAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        subscriptions.add(RxAdapterView.itemSelections(raceSpinner)
                .observeOn(Schedulers.io())
                .skip(1)
                .subscribe(pos -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .race(raceEnumAdapter.getItem(pos))
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
                }));

        subscriptions.add(RxAdapterView.itemSelections(partySpinner)
                .observeOn(Schedulers.io())
                .skip(1)
                .subscribe(pos -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .party(partyEnumAdapter.getItem(pos))
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
                        .value(driversLicense.editable().toString())
                        .attestNoSuchId(checkChange)
                        .build()).observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(contentValues -> {
                    VoterId.insertOrUpdate(db, rockyRequestRowId.get(), DRIVERS_LICENSE, contentValues);
                }));

    }

    /**
     * listen for this here so the db update can happen on a separate thread
     *
     * @param checked
     */
    @OnCheckedChanged(R.id.drivers_license_checkbox)
    public void onCheckChanged(boolean checked) {
        driversLicenseTIL.setVisibility(checked ? View.VISIBLE : View.GONE);
        hasDriversLicenseChecked.onNext(checked);
    }


    @Override
    public Observable<Boolean> verify() {
        if (hasDriversLicense.isChecked()) {
            return validator.validate();
        } else {
            return super.verify();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

}
