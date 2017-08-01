package com.rockthevote.grommet.data;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mechanical Man, LLC on 8/1/17. Grommet
 */

public interface HockeyAppHelper {

    void checkForUpdates(AppCompatActivity activity);

    void checkForCrashes(AppCompatActivity activity);

    void unRegister();
}
