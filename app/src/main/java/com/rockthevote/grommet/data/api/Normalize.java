package com.rockthevote.grommet.data.api;

import com.squareup.moshi.JsonQualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Aaron Huttner on 9/23/16. Grommet
 */

@Retention(RetentionPolicy.RUNTIME)
@JsonQualifier
public @interface Normalize {
}
