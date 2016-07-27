package com.rockthevote.grommet.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    overrides = true,
    library = true,
    complete = false
)
public final class InternalReleaseUiModule {
  @Provides @Singleton ViewContainer provideViewContainer(
      TelescopeViewContainer telescopeViewContainer) {
    return telescopeViewContainer;
  }
}
