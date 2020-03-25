package com.rockthevote.grommet.data;

import android.app.Application;

import com.rockthevote.grommet.data.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Mechanical Man on 3/24/20.
 */

@Module(
        complete = false,
        library = true,
        overrides = true
)

public class ProdDataModule {
    
    @Provides
    @Singleton
    AppDatabase provideAppDatabase(Application application) {
        return AppDatabase.Companion.getInstance(application);
    }
}
