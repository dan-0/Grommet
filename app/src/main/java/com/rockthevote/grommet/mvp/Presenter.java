package com.rockthevote.grommet.mvp;

public interface Presenter<V> {

    void attachView(V view);

    void detachView();

    void destroy();
}
