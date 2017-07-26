package com.rockthevote.grommet.ui.eventFlow;

import com.rockthevote.grommet.data.db.model.Session;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public interface EventFlowCallback {

    void setState(Session.SessionStatus state, boolean smoothScroll);
}
