package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.PartnerNameResponse;

public enum MockPartnerNameResponse {
    SUCCESS("Success", MockPartnerNames.SUCCESS);

    public final String name;
    public final PartnerNameResponse response;

    MockPartnerNameResponse(String name, PartnerNameResponse response) {
        this.name = name;
        this.response = response;
    }

    @Override
    public String toString() {
        return name;
    }
}
