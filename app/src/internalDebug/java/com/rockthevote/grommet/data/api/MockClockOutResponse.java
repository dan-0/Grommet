package com.rockthevote.grommet.data.api;

/**
 * Created by Mechanical Man, LLC on 7/13/17. Grommet
 */

public enum MockClockOutResponse {

    SUCCESS("Success"), FAILURE("Failure");

    public final String name;

    MockClockOutResponse(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
