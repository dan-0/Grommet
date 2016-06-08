package com.rockthevote.grommet.mvp;

import android.support.annotation.NonNull;

public interface PresenterFactory<P extends Presenter> {
    @NonNull P createPresenter();
}
