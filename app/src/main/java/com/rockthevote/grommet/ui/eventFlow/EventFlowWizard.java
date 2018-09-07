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
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.NEW_SESSION;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.PARTNER_UPDATE;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public class EventFlowWizard extends FrameLayout implements EventFlowCallback {

    @Inject BriteDatabase db;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;
    @Inject @PartnerId Preference<String> partnerIdPref;

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
            viewPager.setOffscreenPageLimit(3);

            //disable scrolling on the view pager
            viewPager.setOnTouchListener((v, event) -> true);

            new Handler().post(() -> setState(getStatus(db), false));
        }
    }

    private Session.SessionStatus getStatus(BriteDatabase db) {

        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        int rows = cursor.getCount();
        cursor.close();

        if (Strings.isBlank(partnerIdPref.get()) || rows == 0) {
            return PARTNER_UPDATE;
        } else {

            cursor = db.query(Session.SELECT_CURRENT_SESSION);
            cursor.moveToNext();
            Session session = Session.MAPPER.call(cursor);
            cursor.close();

            return session.sessionStatus();
        }
    }

    @Override
    public void setState(SessionStatus status, boolean smoothScroll) {

        // unregister the current page from callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .unregisterCallbackListener();

        switch (status) {
            case PARTNER_UPDATE:
                viewPager.setCurrentItem(0, smoothScroll);
                break;
            case SESSION_CLEARED:
                // dummy state to let us clear entry fields in the editable page
                viewPager.setCurrentItem(1, smoothScroll);
                ((EventDetailsEditable) adapter.getPageAtPosition(1)).resetForm();
                break;
            case NEW_SESSION:
                // show the event details editable screen
                viewPager.setCurrentItem(1, smoothScroll);
                break;
            case DETAILS_ENTERED: // fall through
            case CLOCKED_IN:
                // show the event details static page along with the clock-out option
                // add location data
                viewPager.setCurrentItem(2, smoothScroll);
                ((SessionTimeTracking) adapter.getPageAtPosition(2)).updateUI(getStatus(db));
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
                viewPager.setCurrentItem(3, smoothScroll);
                ((SessionSummary) adapter.getPageAtPosition(3)).updateUI();
                break;
        }

        // register the current page for callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .registerCallbackListener(this);
    }


}
