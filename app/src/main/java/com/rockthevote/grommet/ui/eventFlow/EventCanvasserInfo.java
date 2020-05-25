package com.rockthevote.grommet.ui.eventFlow;


import android.app.Activity;
import android.content.Context;
import com.google.android.material.textfield.TextInputLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.DeviceID;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.ui.misc.ObservableValidator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

import static com.rockthevote.grommet.data.db.model.SessionStatus.DETAILS_ENTERED;
import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;


public class EventCanvasserInfo extends LinearLayout implements EventFlowPage {

    @Inject RockyService rockyService;

    @Inject @CanvasserName Preference<String> canvasserNamePref;
    @Inject @EventName Preference<String> eventNamePref;
    @Inject @EventZip Preference<String> eventZipPref;
    @Inject @PartnerName Preference<String> partnerNamePref;
    @Inject @DeviceID Preference<String> deviceIdPref;

    @BindView(R.id.ede_canvasser_name) EditText edeCanvasserName;
    @BindView(R.id.ede_event_name) EditText edeEventName;

    @Pattern(regex = "^[0-9]{5}(?:-[0-9]{4})?$", messageResId = R.string.zip_code_error)
    @BindView(R.id.ede_til_event_zip) TextInputLayout edeEventZipTIL;
    @BindView(R.id.ede_event_zip) EditText edeEventZip;
    @BindView(R.id.ede_partner_name) TextView edePartnerName;

    @NotEmpty
    @BindView(R.id.ede_til_device_id) TextInputLayout edeDeviceIdTIL;
    @BindView(R.id.ede_device_id) TextView edeDeviceId;


    private CompositeDisposable disposables = new CompositeDisposable();

    private ObservableValidator validator;

    private EventFlowCallback listener;

    public EventCanvasserInfo(Context context) {
        this(context, null);
    }

    public EventCanvasserInfo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventCanvasserInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_canvasser_info, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
            validator = new ObservableValidator(this, getContext());

            disposables.add(partnerNamePref.asObservable()
                    .subscribe(name -> edePartnerName.setText(name)));

            resetForm();
        }
    }

    void resetForm() {
        //TODO can these be disposables?
        edeCanvasserName.setText(canvasserNamePref.get());
        edeEventName.setText(eventNamePref.get());
        edeEventZip.setText(eventZipPref.get());
        edeCanvasserName.requestFocus();
        edeDeviceId.setText(deviceIdPref.get());
    }

    @OnClick(R.id.event_update_partner_id)
    public void onClickUpdatePartner(View v) {
        listener.setState(PARTNER_UPDATE, true);
    }

    @OnClick(R.id.event_details_save)
    public void onClickSaveDetails(View v) {

        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // allow the user to not set a partner ID
        if (validator.validate().toBlocking().single()) {
            updateSessionData();
        }
    }

    private void updateSessionData() {

        canvasserNamePref.set(edeCanvasserName.getText().toString());
        eventNamePref.set(edeEventName.getText().toString());
        eventZipPref.set(edeEventZip.getText().toString());
        deviceIdPref.set(edeDeviceId.getText().toString());

        listener.setState(DETAILS_ENTERED, true);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            disposables.dispose();
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
}
