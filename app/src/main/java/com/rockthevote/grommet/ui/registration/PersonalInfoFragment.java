package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.f2prateek.rx.preferences.Preference;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.views.AddressView;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;


public class PersonalInfoFragment extends BaseRegistrationFragment {

    @Checked(value = false, messageResId = R.string.error_no_address)
    @BindView(R.id.no_address_checkbox) CheckBox noAddress;

    @BindView(R.id.home_address) AddressView homeAddress;

    @BindView(R.id.mailing_address) AddressView mailingAddress;

    @BindView(R.id.previous_address) AddressView previousAddress;

    @BindView(R.id.mailing_address_is_different) CheckBox mailingAddressIsDifferent;

    @BindView(R.id.address_changed) CheckBox addressChanged;

    @BindView(R.id.mailing_address_divider) View mailingDivider;

    @BindView(R.id.previous_address_divider) View prevDivider;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private ObservableValidator validator;

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
    }

    @OnCheckedChanged(R.id.mailing_address_is_different)
    public void onMailingAddressDifferentChecked(boolean checked) {
        mailingAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        mailingDivider.setVisibility(checked ? View.VISIBLE : View.GONE);

        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .hasMailingAddress(checked)
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
    }

    @OnCheckedChanged(R.id.address_changed)
    public void onAddressChangeChecked(boolean checked) {
        previousAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        prevDivider.setVisibility(checked ? View.VISIBLE : View.GONE);

        db.update(RockyRequest.TABLE,
                new RockyRequest.Builder()
                        .hasPreviousAddress(checked)
                        .build(),
                RockyRequest._ID + " = ? ", String.valueOf(rockyRequestRowId.get()));
    }

    @Override
    public Observable<Boolean> verify() {

        Observable<Boolean> mailingAddressObs = mailingAddress.verify()
                .flatMap(val -> Observable.just(mailingAddressIsDifferent.isChecked() ? val : true));

        Observable<Boolean> changedAddObs = previousAddress.verify()
                .flatMap(val -> Observable.just(addressChanged.isChecked() ? val : true));

        return Observable.zip(homeAddress.verify(), mailingAddressObs,
                changedAddObs, validator.validate(),
                (home, mail, change, other) -> home && mail && change && other);

    }
}
