package com.rockthevote.grommet.data.api;

import com.ryanharter.auto.value.moshi.MoshiAdapterFactory;
import com.squareup.moshi.JsonAdapter;


@MoshiAdapterFactory
public abstract class RockyAdapterFactory implements JsonAdapter.Factory{

    // Static factory method to access the package
    // private generated implementation
    public static JsonAdapter.Factory create() {
        return new AutoValueMoshi_RockyAdapterFactory();
    }
}
