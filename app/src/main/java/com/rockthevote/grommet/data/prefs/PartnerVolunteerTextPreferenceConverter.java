package com.rockthevote.grommet.data.prefs;

import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.squareup.moshi.JsonAdapter;

import java.io.IOException;

/**
 * Created by Mechanical Man on 1/15/18.
 */

public class PartnerVolunteerTextPreferenceConverter implements Preference.Converter<PartnerVolunteerText> {
    private final JsonAdapter<PartnerVolunteerText> adapter;

    public PartnerVolunteerTextPreferenceConverter(JsonAdapter<PartnerVolunteerText> adapter) {
        this.adapter = adapter;
    }

    @NonNull
    @Override
    public PartnerVolunteerText deserialize(@NonNull String serialized) {

        PartnerVolunteerText value = PartnerVolunteerText.builder()
                .english("")
                .spanish("")
                .build();

        try {
            value = adapter.fromJson(serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    @NonNull
    @Override
    public String serialize(@NonNull PartnerVolunteerText value) {
        return adapter.toJson(value);
    }
}
