package com.rockthevote.grommet.ui.eventFlow;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.util.Dates;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.NEW_SESSION;
import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.SESSION_CLEARED;

public class SessionSummary extends FrameLayout implements EventFlowPage {

    @Inject BriteDatabase db;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    @Inject @CanvasserName Preference<String> canvasserNamePref;
    @Inject @EventName Preference<String> eventNamePref;
    @Inject @EventZip Preference<String> eventZipPref;
    @Inject @PartnerId Preference<String> partnerIdPref;
    @Inject @PartnerName Preference<String> partnerNamePref;

    @BindView(R.id.summary_canvasser_name) TextView edCanvasserName;
    @BindView(R.id.summary_event_name) TextView edEventName;
    @BindView(R.id.summary_event_zip) TextView edEventZip;
    @BindView(R.id.summary_partner_name) TextView edPartnerName;

    @BindView(R.id.summary_clock_in_time) TextView clockInTime;
    @BindView(R.id.summary_clock_out_time) TextView clockoutTime;
    @BindView(R.id.summary_total_time) TextView totalTime;


    private CompositeSubscription subscriptions = new CompositeSubscription();

    private EventFlowCallback listener;

    public SessionSummary(Context context) {
        this(context, null);
    }

    public SessionSummary(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionSummary(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.session_summary, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);

            subscriptions.add(canvasserNamePref.asObservable()
                    .subscribe(name -> edCanvasserName.setText(name)));

            subscriptions.add(eventNamePref.asObservable()
                    .subscribe(name -> edEventName.setText(name)));

            subscriptions.add(eventZipPref.asObservable()
                    .subscribe(name -> edEventZip.setText(name)));

            subscriptions.add(partnerNamePref.asObservable()
                    .subscribe(name -> edPartnerName.setText(name)));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            subscriptions.unsubscribe();
        }
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        listener = null;
    }

    @SuppressLint("DefaultLocale")
    void updateUI() {
        Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
        cursor.moveToNext();
        Session session = Session.MAPPER.call(cursor);
        cursor.close();

        clockInTime.setText(Dates.formatAs_LocalTimeOfDay(session.clockInTime()));
        clockoutTime.setText(Dates.formatAs_LocalTimeOfDay(session.clockOutTime()));

        Date in = session.clockInTime();
        Date out = session.clockOutTime();
        if (null != out && null != in) {
            long elapsedMilliseconds = out.getTime() - in.getTime();

            String elapsedTime = String.format("%d hours, %d min",
                    TimeUnit.MILLISECONDS.toHours(elapsedMilliseconds),
                    TimeUnit.MILLISECONDS.toMinutes(elapsedMilliseconds) -
                            TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMilliseconds))
            );

            totalTime.setText(elapsedTime);
        }
    }

    @OnClick(R.id.session_summary_clear)
    public void onClickClear(View v) {
        // Clear visible session data
        canvasserNamePref.delete();
        eventNamePref.delete();
        eventZipPref.delete();
        partnerIdPref.delete();
        partnerNamePref.delete();

        // update session status
        Session.Builder updateBuilder = new Session.Builder()
                .clockInTime(new GregorianCalendar().getTime())
                .sessionStatus(NEW_SESSION);

        db.update(Session.TABLE,
                updateBuilder.build(),
                Session._ID + " = ? ", String.valueOf(currentSessionRowId.get()));

        // create a new blank session
        Session.Builder newSessionBuilder = new Session.Builder()
                .sessionStatus(NEW_SESSION)
                .sessionId(UUID.randomUUID().toString());

        long rowId = db.insert(Session.TABLE, newSessionBuilder.build());
        currentSessionRowId.set(rowId);

        // delete sessions that have already been reported
        int rowsDeleted = db.delete(Session.TABLE, Session.DELETE_REPORTED_ROWS_WHERE_CLAUSE);

        Timber.d("deleted %d session rows", rowsDeleted);

        // update wizard view pager to SESSION_CLEARED to tell the first page to reset
        listener.setState(SESSION_CLEARED, false);
    }

}
