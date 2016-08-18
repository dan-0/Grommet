package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class AssistantInfoFragment extends BaseRegistrationFragment {

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

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
    }
}
