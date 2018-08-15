package com.rockthevote.grommet;

import com.rockthevote.grommet.data.api.PracticeApiModule;

import dagger.Module;

@Module(
        addsTo = GrommetModule.class,
        includes = {
                PracticeApiModule.class
        },
        overrides = true
)
public final class PracticeGrommetModule {
}
