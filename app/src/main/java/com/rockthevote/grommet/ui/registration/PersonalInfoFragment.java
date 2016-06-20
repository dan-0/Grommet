package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.Spinner;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.api.model.PhoneType;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.NameView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class PersonalInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.personal_info_scrollview) NestedScrollView nestedScrollView;
    @BindView(R.id.personal_info_gridlayout) GridLayout gridLayout;
    @BindView(R.id.spinner_phone_type) Spinner spinnerPhoneType;
    @BindView(R.id.mailing_address_is_different) CheckBox mailingAddressIsDifferent;

    @BindView(R.id.home_address) AddressView homeAddress;
    @BindView(R.id.mailing_address) AddressView mailingAddress;
    @BindView(R.id.previous_address) AddressView previousAddress;

    @BindView(R.id.previous_name) NameView previousName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setContentView(R.layout.fragment_personal_info);
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, v);
        setupSpinner();
        return v;
    }

    private void setupSpinner() {
        final EnumAdapter<PhoneType> phoneTypeEnumAdapter = new EnumAdapter<>(getActivity(), PhoneType.class);
        spinnerPhoneType.setAdapter(phoneTypeEnumAdapter);

    }

    @OnCheckedChanged(R.id.mailing_address_is_different)
    public void onMailingAddressDifferentChecked(boolean checked) {
        mailingAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnCheckedChanged(R.id.address_changed)
    public void onAddressChangeChecked(boolean checked) {
        previousAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnCheckedChanged(R.id.name_changed)
    public void onNameChangedChecked(boolean checked) {
        previousName.setVisibility(checked ? View.VISIBLE : View.GONE);
    }
}
