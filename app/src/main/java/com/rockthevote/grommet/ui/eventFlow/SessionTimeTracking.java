package com.rockthevote.grommet.ui.eventFlow;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.data.db.model.SessionStatus;
import com.rockthevote.grommet.util.Dates;

import javax.inject.Inject;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rockthevote.grommet.data.db.model.SessionStatus.CLOCKED_IN;
import static com.rockthevote.grommet.data.db.model.SessionStatus.CLOCKED_OUT;
import static com.rockthevote.grommet.data.db.model.SessionStatus.NEW_SESSION;

public class SessionTimeTracking extends FrameLayout implements EventFlowPage {

    @Inject PartnerInfoDao partnerInfoDao;
    @Inject SessionDao sessionDao;
    @Inject RegistrationDao registrationDao;
    @Inject SharedPreferences sharedPreferences;
    @Inject RockyService rockyService;

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
                new SessionTimeTrackingViewModelFactory(
                        partnerInfoDao,
                        sessionDao,
                        registrationDao,
                        sharedPreferences,
                        rockyService)
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
                    } else {
                        clockInTime.setText(R.string.time_tracking_default_value);
                    }
                });

        viewModel.getSessionStatus().observe((AppCompatActivity) getContext(), this::updateUI);

        viewModel.getClockState().observe((AppCompatActivity) getContext(), clockState -> {
            if (clockState instanceof ClockEvent.ClockingError) {
                displayClockEventDialog(((ClockEvent.ClockingError) clockState).getErrorMsgId());
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
        switch (status) {
            case CLOCKED_IN: {
                editButton.setEnabled(false);
                clockInButton.setSelected(true);
                runClockInAnimation();

                TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
                text.setText(R.string.clock_in_text);
                listener.setState(CLOCKED_IN, true);
                break;
            }
            case CLOCKED_OUT: {
                editButton.setEnabled(true);
                clockInButton.setSelected(false);
                TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
                text.setText(R.string.clock_out_text);
                listener.setState(CLOCKED_OUT, true);
                break;
            }
            default: {
                editButton.setEnabled(true);
                clockInButton.setSelected(false);
                clockInTime.setText(R.string.time_tracking_default_value);
                TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
                text.setText(R.string.clock_in_text);
                break;
            }
        }
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

            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.clock_out_dialog_text)
                    .setPositiveButton(R.string.dialog_yes, (dialogInterface, i) -> {
                        viewModel.clockOut();
                    })
                    .setNegativeButton(R.string.dialog_no,
                            (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();
        } else {
            viewModel.clockIn();
        }
    }

    private void runClockInAnimation() {
        TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
        AnimatorSet animSet = new AnimatorSet();

        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                getResources().getDimension(R.dimen.clock_in_transition_width),
                getResources().getDisplayMetrics());

        ValueAnimator shrinkAnim = ValueAnimator.ofInt(clockInButton.getMeasuredWidth(), width);

        shrinkAnim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = clockInButton.getLayoutParams();
            layoutParams.width = val;
            clockInButton.requestLayout();
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

    private void displayClockEventDialog(@StringRes int msgId) {
        new AlertDialog.Builder(getContext())
                .setMessage(msgId)
                .setPositiveButton(R.string.action_ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create()
                .show();
    }
}
