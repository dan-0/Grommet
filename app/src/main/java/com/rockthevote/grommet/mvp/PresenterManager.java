package com.rockthevote.grommet.mvp;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A fantastic and simple MVP pattern used with permission and developed by Michael Evans
 * github: https://github.com/MichaelEvans
 * website: http://michaelevans.org/
 */

@Singleton
public class PresenterManager {

    private final Map<Class, Presenter> presenterMap;

    @Inject
    public PresenterManager() {
        presenterMap = new HashMap<>();
    }

    public <P extends Presenter> P getOrCreatePresenter(Class<P> clazz, PresenterFactory<P> presenterFactory) {
        if (!presenterMap.containsKey(clazz)) {
            P presenter = presenterFactory.createPresenter();
            presenterMap.put(clazz, presenter);
            return presenter;
        } else {
            return (P) presenterMap.get(clazz);
        }
    }

    public void remove(Class c) {
        Presenter presenter = presenterMap.get(c);
        if (presenter != null) {
            presenter.destroy();
        }
        presenterMap.remove(c);
    }


}
