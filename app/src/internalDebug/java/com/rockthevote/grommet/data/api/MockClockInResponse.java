package com.rockthevote.grommet.data.api;

/**
 * Created by Mechanical Man, LLC on 7/13/17. Grommet
 */

public enum MockClockInResponse {

    SUCCESS("Success"), FAILURE("Failure");

    public final String name;

    MockClockInResponse(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
