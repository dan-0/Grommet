package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;

import java.util.Calendar;

public final class MockPartnerNames {
    static final PartnerNameResponse SUCCESS = PartnerNameResponse.builder()
            .isValid(true)
            .partnerName("OSET Org")
            .sessionTimeoutLength(10)
            .registrationDeadlineDate(Calendar.getInstance().getTime())
            .registrationNotificationText(RegistrationNotificationText.builder()
                    .english("enlish text")
                    .spanish("spanish text")
                    .build())
            .build();
}
