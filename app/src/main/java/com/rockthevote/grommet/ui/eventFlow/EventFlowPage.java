package com.rockthevote.grommet.ui.eventFlow;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public interface EventFlowPage {

    void registerCallbackListener(EventFlowCallback listener);

    void unregisterCallbackListener();

}
