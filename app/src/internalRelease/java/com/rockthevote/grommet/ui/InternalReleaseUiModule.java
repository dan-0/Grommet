package com.rockthevote.grommet.ui;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

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
