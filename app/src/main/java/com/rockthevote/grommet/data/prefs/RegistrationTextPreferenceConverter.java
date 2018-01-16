package com.rockthevote.grommet.data.prefs;

import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.squareup.moshi.JsonAdapter;

import java.io.IOException;

/**
 * Created by Mechanical Man on 1/15/18.
 */

public class RegistrationTextPreferenceConverter implements Preference.Converter<RegistrationNotificationText> {
    private final JsonAdapter<RegistrationNotificationText> adapter;

    public RegistrationTextPreferenceConverter(JsonAdapter<RegistrationNotificationText> adapter) {
        this.adapter = adapter;
    }

    @NonNull
    @Override
    public RegistrationNotificationText deserialize(@NonNull String serialized) {

        RegistrationNotificationText value = RegistrationNotificationText.builder()
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
    public String serialize(@NonNull RegistrationNotificationText value) {
        return adapter.toJson(value);
    }
}
