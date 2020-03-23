package com.rockthevote.grommet.data.prefs;

import androidx.annotation.NonNull;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.data.api.model.DateAdapter;

import java.util.Date;

/**
 * Created by Mechanical Man on 1/15/18.
 */

public class RegistrationDeadlinePreferenceConverter implements Preference.Converter<Date> {

    @NonNull
    @Override
    public Date deserialize(@NonNull String serialized) {
        // this way we make sure we're using the same conversion method
        return new DateAdapter().fromJson(serialized);
    }

    @NonNull
    @Override
    public String serialize(@NonNull Date value) {
        return new DateAdapter().toJson(value);
    }
}
