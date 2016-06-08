package com.rockthevote.grommet.ui.registration;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.api.model.PhoneType;
import com.rockthevote.grommet.data.api.model.Suffix;
import com.rockthevote.grommet.data.api.model.Title;
import com.rockthevote.grommet.ui.misc.EnumAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonalInfoFragment extends Fragment {

    @BindView(R.id.spinner_title) Spinner spinnerTitle;
    @BindView(R.id.spinner_suffix) Spinner spinnerSuffix;
    @BindView(R.id.spinner_phone_type) Spinner spinnerPhoneType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.personal_info, container, false);
        ButterKnife.bind(this, v);
        setupSpinners();
        return v;
    }

    private void setupSpinners() {
        final EnumAdapter<Title> titleEnumAdapter = new EnumAdapter<>(getActivity(), Title.class);
        spinnerTitle.setAdapter(titleEnumAdapter);

        final EnumAdapter<Suffix> suffixEnumAdapter = new EnumAdapter<>(getActivity(), Suffix.class);
        spinnerSuffix.setAdapter(suffixEnumAdapter);

        final EnumAdapter<PhoneType> phoneTypeEnumAdapter = new EnumAdapter<>(getActivity(), PhoneType.class);
        spinnerPhoneType.setAdapter(phoneTypeEnumAdapter);


    }
}
