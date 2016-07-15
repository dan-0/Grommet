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
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.model.PhoneType;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.NameView;
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
import static com.rockthevote.grommet.data.db.model.ContactMethod.Type.PHONE;


public class PersonalInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.name) NameView name;
    @BindView(R.id.name_changed) CheckBox nameChanged;
    @BindView(R.id.previous_name_divider) View previousNameDivider;
    @BindView(R.id.previous_name) NameView previousName;

    @BindView(R.id.home_address) AddressView homeAddress;
    @BindView(R.id.mailing_address) AddressView mailingAddress;
    @BindView(R.id.previous_address) AddressView previousAddress;
    @BindView(R.id.mailing_address_is_different) CheckBox mailingAddressIsDifferent;
    @BindView(R.id.address_changed) CheckBox addressChanged;
    @BindView(R.id.phone) EditText phone;

    @NotEmpty
    @BindView(R.id.til_phone_number) TextInputLayout phoneNumber;
    @BindView(R.id.spinner_phone_type) Spinner spinnerPhoneType;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private EnumAdapter<PhoneType> phoneTypeEnumAdapter;

    private ObservableValidator validator;

    private CompositeSubscription subscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_personal_info);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        validator = new ObservableValidator(this, getActivity());

        phoneTypeEnumAdapter = new EnumAdapter<>(getActivity(), PhoneType.class);
        spinnerPhoneType.setAdapter(phoneTypeEnumAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

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

        //TODO make sure and set a default phone type or have it be required that they select an option
        subscriptions.add(RxAdapterView.itemSelections(spinnerPhoneType)
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(2)
                .subscribe(pos -> {
                    db.update(RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .phoneType(phoneTypeEnumAdapter.getItem(pos))
                                    .build(),
                            RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));

                }));

    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.unsubscribe();
    }

    @OnCheckedChanged(R.id.mailing_address_is_different)
    public void onMailingAddressDifferentChecked(boolean checked) {
        mailingAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .regIsMail(!checked)
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
    }

    @OnCheckedChanged(R.id.address_changed)
    public void onAddressChangeChecked(boolean checked) {
        previousAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnCheckedChanged(R.id.name_changed)
    public void onNameChangedChecked(boolean checked) {
        previousName.setVisibility(checked ? View.VISIBLE : View.GONE);
        previousNameDivider.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @Override
    public Observable<Boolean> verify() {

        Observable<Boolean> previousNameObs = previousName.verify()
                .flatMap(val -> Observable.just(nameChanged.isChecked() ? val : true));

        Observable<Boolean> mailingAddressObs = mailingAddress.verify()
                .flatMap(val -> Observable.just(mailingAddressIsDifferent.isChecked() ? val : true));

        Observable<Boolean> changedAddObs = previousAddress.verify()
                .flatMap(val -> Observable.just(addressChanged.isChecked() ? val : true));

        //TODO instead of concat I could do a toBlocking and then do an Observable.just(foo,bar,...)
        return validator.validate()
                .concatWith(name.verify())
                .concatWith(previousNameObs)
                .concatWith(homeAddress.verify())
                .concatWith(mailingAddressObs)
                .concatWith(changedAddObs)
                .toList()
                .flatMap(list -> {
                    Boolean ret = true;
                    for (Boolean val : list) {
                        ret = ret && val;
                    }

                    return Observable.just(ret);
                });

    }
}
