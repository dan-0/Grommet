package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.PartnerNameResponse;

public final class MockPartnerNames {
    static final PartnerNameResponse SUCCESS = PartnerNameResponse.builder()
            .isValid(true)
            .partnerName("OSET Org")
            .sessionTimeoutLength(10)
            .build();
}
