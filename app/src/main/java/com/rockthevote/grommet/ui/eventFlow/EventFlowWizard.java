package com.rockthevote.grommet.ui.eventFlow;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.db.model.Session.SessionStatus;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.CLOCKED_IN;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.DETAILS_ENTERED;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.SESSION_CLEARED;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public class EventFlowWizard extends FrameLayout implements EventFlowCallback {

    @Inject BriteDatabase db;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    @BindView(R.id.viewpager) ViewPager viewPager;

    private EventDetailFlowAdapter adapter;

    public EventFlowWizard(Context context) {
        this(context, null);
    }

    public EventFlowWizard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventFlowWizard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_flow_wizard, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);

            adapter = new EventDetailFlowAdapter(getContext());
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(2);

            //disable scrolling on the view pager
            viewPager.setOnTouchListener((v, event) -> true);

            new Handler().post(() -> setState(getStatus(), false));
        }
    }

    public SessionStatus getStatus() {

        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        int rows = cursor.getCount();
        cursor.close();

        // if the session table is empty then create a new session
        if (rows == 0) {
            Session.Builder builder = new Session.Builder()
                    .sessionStatus(SESSION_CLEARED)
                    .sessionId(UUID.randomUUID().toString());

            long rowId = db.insert(Session.TABLE, builder.build());
            currentSessionRowId.set(rowId);
        }

        cursor = db.query(Session.SELECT_CURRENT_SESSION);
        cursor.moveToNext();
        Session session = Session.MAPPER.call(cursor);
        cursor.close();

        return session.sessionStatus();
    }

    @Override
    public void setState(SessionStatus status, boolean smoothScroll) {

        // unregister the current page from callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .unregisterCallbackListener();

        switch (status) {
            case SESSION_CLEARED:
                // dummy state to let us clear entry fields in the editable page
                viewPager.setCurrentItem(0, smoothScroll);
                ((EventDetailsEditable) adapter.getPageAtPosition(0)).resetForm();
            case NEW_SESSION:
                // show the event details editable screen
                // clear session data and reset other views (like the clock out view)
                viewPager.setCurrentItem(0, smoothScroll);
                break;
            case DETAILS_ENTERED:
                // show the event details static page along with the clock-in option
                viewPager.setCurrentItem(1, smoothScroll);
                ((SessionTimeTracking) adapter.getPageAtPosition(1)).updateUI(DETAILS_ENTERED);
                break;
            case CLOCKED_IN:
                // show the event details static page along with the clock-out option
                // add location data
                viewPager.setCurrentItem(1, smoothScroll);
                ((SessionTimeTracking) adapter.getPageAtPosition(1)).updateUI(CLOCKED_IN);
                break;
            case TIMED_OUT:
                new android.app.AlertDialog.Builder(getContext())
                        .setIcon(R.drawable.ic_warning_24dp)
                        .setTitle(R.string.shift_timeout_title)
                        .setMessage(R.string.shift_timeout_message)
                        .setPositiveButton(R.string.action_ok,
                                (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            case CLOCKED_OUT:
                // show session summary
                viewPager.setCurrentItem(2, smoothScroll);
                ((SessionSummary) adapter.getPageAtPosition(2)).updateUI();
                break;
        }

        // register the current page for callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .registerCallbackListener(this);
    }


}
