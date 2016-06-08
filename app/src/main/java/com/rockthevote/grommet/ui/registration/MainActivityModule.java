package com.rockthevote.grommet.ui.registration;

import com.rockthevote.grommet.GrommetModule;

import dagger.Module;

@Module(
        addsTo = GrommetModule.class
)
public final class MainActivityModule {
    private final MainActivity mainActivity;

    MainActivityModule(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

//    @Provides
//    @Singleton
//    DrawerLayout provideDrawerLayout() {
//        return mainActivity.drawerLayout;
//    }
}