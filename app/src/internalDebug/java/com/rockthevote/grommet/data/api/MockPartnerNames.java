package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.util.Dates;

import java.util.Calendar;

public final class MockPartnerNames {
    static final PartnerNameResponse SUCCESS = PartnerNameResponse.builder()
            .isValid(true)
            .partnerName("OSET Org")
            .sessionTimeoutLength(10)
            .registrationDeadlineDate(Dates.parseISO8601_ShortDate("2018-09-01"))
            .registrationNotificationText(RegistrationNotificationText.builder()
                    .english("enlish text")
                    .spanish("spanish text")
                    .build())
            .build();
}
