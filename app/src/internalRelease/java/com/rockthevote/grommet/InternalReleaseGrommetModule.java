package com.rockthevote.grommet;

import com.rockthevote.grommet.data.InternalReleaseDataModule;
import com.rockthevote.grommet.ui.InternalReleaseUiModule;

import dagger.Module;

@Module(
        addsTo = GrommetModule.class,
        includes = {
                InternalReleaseUiModule.class,
                InternalReleaseDataModule.class
        },
        overrides = true
)
public final class InternalReleaseGrommetModule {
}
