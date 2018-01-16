package com.rockthevote.grommet.data.api.model;

import com.rockthevote.grommet.util.Dates;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Calendar;
import java.util.Date;

import static com.rockthevote.grommet.util.Dates.parseISO8601_ShortDate;

/**
 * Created by Mechanical Man on 1/16/18.
 */

public class DateAdapter {

    @ToJson
    public String toJson(Date date) {
        return Dates.formatAsISO8601_ShortDate(date);
    }

    @FromJson
    public Date fromJson(String date) {
        Date value = parseISO8601_ShortDate(date);
        if (value == null) {
            return Calendar.getInstance().getTime();
        } else {
            return value;
        }
    }
}
