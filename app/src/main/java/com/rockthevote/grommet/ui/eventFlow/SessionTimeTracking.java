package com.rockthevote.grommet.ui.eventFlow;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.data.db.model.SessionStatus;
import com.rockthevote.grommet.ui.misc.AnimatorListenerHelper;
import com.rockthevote.grommet.util.Dates;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.Unit;

import static com.rockthevote.grommet.data.db.model.SessionStatus.CLOCKED_IN;
import static com.rockthevote.grommet.data.db.model.SessionStatus.CLOCKED_OUT;
import static com.rockthevote.grommet.data.db.model.SessionStatus.NEW_SESSION;

public class SessionTimeTracking extends FrameLayout implements EventFlowPage {

    @Inject PartnerInfoDao partnerInfoDao;
    @Inject SessionDao sessionDao;
    @Inject RegistrationDao registrationDao;

    @BindView(R.id.ed_canvasser_name) TextView edCanvasserName;
    @BindView(R.id.ed_event_name) TextView edEventName;
    @BindView(R.id.ed_event_zip) TextView edEventZip;
    @BindView(R.id.ed_partner_name) TextView edPartnerName;
    @BindView(R.id.clock_in_button) View clockInButton;
    @BindView(R.id.event_details_static_edit) Button editButton;
    @BindView(R.id.ed_clock_in_time) TextView clockInTime;
    @BindView(R.id.session_progress_button) Button sessionProgressButton;
    @BindView(R.id.ed_device_id) TextView edDeviceId;

    private EventFlowCallback listener;

    private SessionTimeTrackingViewModel viewModel;

    public SessionTimeTracking(Context context) {
        this(context, null);
    }

    public SessionTimeTracking(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionTimeTracking(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.session_time_tracking, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
        }

        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new SessionTimeTrackingViewModelFactory(partnerInfoDao, sessionDao, registrationDao)
        ).get(SessionTimeTrackingViewModel.class);

        observeData();
    }

    private void observeData(){

        viewModel.getSessionData().observe(
                (AppCompatActivity) getContext(), data -> {
                    edCanvasserName.setText(data.getCanvasserName());
                    edEventName.setText(data.getOpenTrackingId());
                    edEventZip.setText(data.getPartnerTrackingId());
                    edPartnerName.setText(data.getPartnerName());
                    edDeviceId.setText(data.getDeviceId());

                    if (data.getClockInTime() != null) {
                        clockInTime.setText(Dates.formatAs_LocalTimeOfDay(data.getClockInTime()));
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        listener = null;
    }

    void updateUI(SessionStatus status) {

        // we can check for this via the DB and if there are no rows, it's fine

        switch (status) {
            case CLOCKED_IN:
                clockInButton.setSelected(true);

                break;
            default:
                clockInButton.setSelected(false);
                clockInTime.setText(R.string.time_tracking_default_value);
                break;
        }

        editButton.setEnabled(!clockInButton.isSelected());

        TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
        text.setText(clockInButton.isSelected() ? R.string.clock_out_text : R.string.clock_in_text);
    }

    @OnClick(R.id.session_progress_button)
    public void onProgressClick(View v) {
        SessionProgressDialogFragment.newInstance()
                .show(((AppCompatActivity) getContext()).getSupportFragmentManager(),
                        "session_progress");
    }

    @OnClick(R.id.event_details_static_edit)
    public void onClickEdit(View v) {
        listener.setState(NEW_SESSION, true);
    }

    @OnClick(R.id.clock_in_button)
    public void onClickClockIn(View button) {
        if (button.isSelected()) {
            viewModel.asyncCanClockOut(this::canClockOut, this::canNotClockOut);
        } else {

            TextView text = (TextView) ((ViewGroup) button).getChildAt(0);
            AnimatorSet animSet = new AnimatorSet();

            int width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDimension(R.dimen.clock_in_transition_width),
                    getResources().getDisplayMetrics());

            ValueAnimator shrinkAnim = ValueAnimator.ofInt(button.getMeasuredWidth(), width);

            shrinkAnim.addUpdateListener(valueAnimator -> {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
                layoutParams.width = val;
                button.requestLayout();
            });

            shrinkAnim.addListener(new AnimatorListenerHelper() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    clockIn();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                    toggleClockInButton();
                }
            });

            ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(text, "alpha", 1f, 0f);
            ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(text, "alpha", 0f, 1f);

            // this is 500 ms each way, so with a "reverse" it's a total of 1 second
            shrinkAnim.setDuration(500);
            shrinkAnim.setRepeatMode(ValueAnimator.REVERSE);
            shrinkAnim.setRepeatCount(1);

            fadeOutAnim.setDuration(200);
            fadeInAnim.setDuration(200);

            animSet.play(fadeOutAnim)
                    .with(shrinkAnim);
            animSet.play(fadeInAnim)
                    .after(fadeOutAnim)
                    .after(750); // wait to fade in so we don't get jittery text

            animSet.start();
        }
    }

    private void toggleClockInButton() {
        clockInButton.setSelected(!clockInButton.isSelected());

        TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
        text.setText(clockInButton.isSelected() ? R.string.clock_out_text : R.string.clock_in_text);
    }

    private Unit canClockOut() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.clock_out_dialog_text)
                .setPositiveButton(R.string.dialog_yes, (dialogInterface, i) -> {
                    clockOut();
                })
                .setNegativeButton(R.string.dialog_no,
                        (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();

        return Unit.INSTANCE;
    }

    private Unit canNotClockOut() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.clock_out_must_upload)
                .setPositiveButton(R.string.action_ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create()
                .show();

        return Unit.INSTANCE;
    }

    private void clockIn() {

        updateUI(CLOCKED_IN);

    }

    private void clockOut() {

        toggleClockInButton();
        listener.setState(CLOCKED_OUT, true);
    }
}
