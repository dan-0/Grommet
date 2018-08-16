package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.util.Dates;

public final class MockPartnerNames {
    static final PartnerNameResponse SUCCESS = PartnerNameResponse.builder()
            .isValid(true)
            .partnerName("OSET Org")
            .sessionTimeoutLength(2)
            .registrationDeadlineDate(Dates.parseISO8601_ShortDate("2018-09-01"))
            .registrationNotificationText(RegistrationNotificationText.builder()
                    .english("english text")
                    .spanish("spanish text")
                    .build())
            .partnerVolunteerText(PartnerVolunteerText.builder()
                    .english("english volunteer text")
                    .spanish("spanish volunteer text")
                    .build())
            .build();
}
