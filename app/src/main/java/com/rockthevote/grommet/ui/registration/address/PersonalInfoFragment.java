package com.rockthevote.grommet.ui.registration.address;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.views.AddressView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;


public class PersonalInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.home_address) AddressView homeAddress;

    @BindView(R.id.mailing_address) AddressView mailingAddress;

    @BindView(R.id.previous_address) AddressView previousAddress;

    @BindView(R.id.mailing_address_is_different) CheckBox mailingAddressIsDifferent;

    @BindView(R.id.address_changed) CheckBox addressChanged;

    @BindView(R.id.mailing_address_divider) View mailingDivider;

    @BindView(R.id.previous_address_divider) View prevDivider;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    @OnCheckedChanged(R.id.mailing_address_is_different)
    public void onMailingAddressDifferentChecked(boolean checked) {
        mailingAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        mailingDivider.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnCheckedChanged(R.id.address_changed)
    public void onAddressChangeChecked(boolean checked) {
        previousAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        prevDivider.setVisibility(checked ? View.VISIBLE : View.GONE);

    }

    @Override
    public Observable<Boolean> verify() {

        Observable<Boolean> mailingAddressObs = mailingAddress.verify()
                .flatMap(val -> Observable.just(mailingAddressIsDifferent.isChecked() ? val : true));

        Observable<Boolean> changedAddObs = previousAddress.verify()
                .flatMap(val -> Observable.just(addressChanged.isChecked() ? val : true));

        return Observable.zip(homeAddress.verify(), mailingAddressObs, changedAddObs,
                (home, mail, change) -> home && mail && change);

    }
}
