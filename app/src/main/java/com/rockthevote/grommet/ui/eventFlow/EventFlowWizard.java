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
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.db.model.Session.SessionStatus;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.DeviceID;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.data.prefs.PartnerVolunteerTextPref;
import com.rockthevote.grommet.data.prefs.RegistrationDeadline;
import com.rockthevote.grommet.data.prefs.RegistrationText;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Date;

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

    // preferences
    @Inject @PartnerId Preference<String> partnerIdPref;
    @Inject @PartnerName Preference<String> partnerNamePref;
    @Inject @RegistrationDeadline Preference<Date> registrationDeadlinePref;
    @Inject @RegistrationText Preference<RegistrationNotificationText> registrationTextPref;
    @Inject @PartnerVolunteerTextPref Preference<PartnerVolunteerText> partnerVolunteerTextPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;
    @Inject @EventName Preference<String> eventNamePref;
    @Inject @EventZip Preference<String> eventZipPref;
    @Inject @DeviceID Preference<String> deviceIdPref;

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

        if (!partnerInfoExists()) {

            // set them on the partner login screen
            return PARTNER_UPDATE;

        } else if (!canvasserInfoExists()) {

            // have them re-enter the info
            return NEW_SESSION;
        } else {

            // return them to the screen the left off at
            SessionStatus status;
            Cursor cursor = db.query(Session.SELECT_CURRENT_SESSION);
            if (cursor.moveToNext()) {
                status = Session.MAPPER.call(cursor).sessionStatus();

            } else {
                status = NEW_SESSION;
            }
            cursor.close();

            return status;
        }
    }

    /**
     * can't check session timeout since 0 is a valid value and it defaults to 0 if there is no value
     * <p>
     * can't check date since it defaults to the current time
     *
     * @return true if all the info from the partner ID validation call exists, false otherwise
     */
    private boolean partnerInfoExists() {
        return !Strings.isBlank(partnerIdPref.get())
                && !Strings.isBlank(partnerNamePref.get())
                && !Strings.isBlank(registrationTextPref.get().english())
                && !Strings.isBlank(partnerVolunteerTextPref.get().english());

    }

    /**
     * @return true of all the canvasser info is filled out, false otherwise
     */
    private boolean canvasserInfoExists() {
        return !Strings.isBlank(canvasserNamePref.get())
                && !Strings.isBlank(eventNamePref.get())
                && !Strings.isBlank(eventZipPref.get())
                && !Strings.isBlank(deviceIdPref.get());

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
