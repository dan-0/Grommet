package com.rockthevote.grommet.ui;


import com.rockthevote.grommet.ui.registration.AdditionalInfoFragment;
import com.rockthevote.grommet.ui.registration.NewRegistrantFragment;
import com.rockthevote.grommet.ui.registration.PersonalInfoFragment;
import com.rockthevote.grommet.ui.registration.RegistrationActivity;
import com.rockthevote.grommet.ui.registration.ReviewAndConfirmFragment;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.EventDetails;
import com.rockthevote.grommet.ui.views.NameView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                InfoActivity.class,
                BaseActivity.class,
                DataUsageActivity.class,
                HelpActivity.class,
                MainActivity.class,
                RegistrationActivity.class,
                NewRegistrantFragment.class,
                PersonalInfoFragment.class,
                AdditionalInfoFragment.class,
                ReviewAndConfirmFragment.class,
                AddressView.class,
                NameView.class,
                EventDetails.class
        },
        complete = false,
        library = true
)
public final class UiModule {
    @Provides
    @Singleton
    ViewContainer provideViewContainer() {
        return ViewContainer.DEFAULT;
    }

    @Provides
    @Singleton
    ActivityHierarchyServer provideActivityHierarchyServer() {
        return ActivityHierarchyServer.NONE;
    }
}

