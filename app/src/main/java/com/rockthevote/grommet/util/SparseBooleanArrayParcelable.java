package com.rockthevote.grommet.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

public class SparseBooleanArrayParcelable extends SparseBooleanArray implements Parcelable {

    public SparseBooleanArrayParcelable(SparseBooleanArray sbArray) {
        for (int i = 0; i < sbArray.size(); i++) {
            put(sbArray.keyAt(i), sbArray.valueAt(i));
        }
    }

    public static final Creator<SparseBooleanArrayParcelable> CREATOR = new Creator<SparseBooleanArrayParcelable>() {
        @Override
        public SparseBooleanArrayParcelable createFromParcel(Parcel in) {
            return new SparseBooleanArrayParcelable(in.readSparseBooleanArray());
        }

        @Override
        public SparseBooleanArrayParcelable[] newArray(int size) {
            return new SparseBooleanArrayParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int x) {
        parcel.writeSparseBooleanArray(this);
    }
}
