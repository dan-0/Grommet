package com.rockthevote.grommet.ui.eventFlow;


import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.f2prateek.rx.preferences.Preference;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.DETAILS_ENTERED;


public class EventDetailsEditable extends FrameLayout implements EventFlowPage {
    static final ButterKnife.Setter<View, Boolean> ENABLED =
            (view, value, index) -> view.setEnabled(value);
    @Inject BriteDatabase db;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;


    @BindViews({R.id.ede_til_canvasser_name, R.id.ede_til_event_name,
                       R.id.ede_til_event_zip, R.id.ede_til_partner_id})
    List<TextInputLayout> editableViews;

    @BindView(R.id.ede_canvasser_name) EditText edeCanvasserName;
    @BindView(R.id.ede_event_name) EditText edeEventName;

    @Pattern(regex = "^[0-9]{5}(?:-[0-9]{4})?$", messageResId = R.string.zip_code_error)
    @BindView(R.id.ede_til_event_zip) TextInputLayout edeEventZipTIL;
    @BindView(R.id.ede_event_zip) EditText edeEventZip;

    @NotEmpty
    @BindView(R.id.ede_til_partner_id) TextInputLayout edePartnerIdTIL;
    @BindView(R.id.ede_partner_id) EditText edePartnerId;

    @Inject @CanvasserName Preference<String> canvasserNamePref;
    @Inject @EventName Preference<String> eventNamePref;
    @Inject @EventZip Preference<String> eventZipPref;
    @Inject @PartnerId Preference<String> partnerIdPref;
    @Inject @PartnerName Preference<String> partnerNamePref;

    @Inject RockyService rockyService;

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private ObservableValidator validator;

    private EventFlowCallback listener;

    public EventDetailsEditable(Context context) {
        this(context, null);
    }

    public EventDetailsEditable(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventDetailsEditable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_details_editable, this);

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
        }
    }

    void resetForm() {
        edeCanvasserName.setText("");
        edeEventName.setText("");
        edeEventZip.setText("");
        edePartnerId.setText("");
        edeCanvasserName.requestFocus();
    }

    @OnClick(R.id.event_details_save)
    public void onClickSaveDetails(View v) {

        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // allow the user to not set a partner ID
        if (validator.validate().toBlocking().single()) {
            if (edePartnerId.getText().toString().equals(partnerIdPref.get())) {
                updateSessionData(partnerNamePref.get(), false, 0);
            } else {
                rockyService.getPartnerName(edePartnerId.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .delay(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(() -> {
                            ButterKnife.apply(editableViews, ENABLED, false);
                        })
                        .doOnCompleted(() -> ButterKnife.apply(editableViews, ENABLED, true))
                        .subscribe(result -> {
                            if (!result.isError() && result.response().isSuccessful()) {
                                PartnerNameResponse partnerNameResponse = result.response().body();
                                if (partnerNameResponse.isValid()) {
                                    updateSessionData(partnerNameResponse.partnerName(),
                                            true,
                                            partnerNameResponse.sessionTimeoutLength());
                                } else {
                                    edePartnerIdTIL.setError(
                                            getContext().getString(R.string.error_partner_id));
                                }
                            } else {
                                edePartnerIdTIL.setError(
                                        getContext().getString(R.string.error_partner_id));
                            }
                        });
            }
        }
    }

    private void updateSessionData(String name, boolean updateTimeout, long timeout) {

        canvasserNamePref.set(edeCanvasserName.getText().toString());
        eventNamePref.set(edeEventName.getText().toString());
        eventZipPref.set(edeEventZip.getText().toString());
        partnerIdPref.set(edePartnerId.getText().toString());
        partnerNamePref.set(name);

        Session.Builder builder = new Session.Builder()
                .partnerTrackingId(edePartnerId.getText().toString())
                .canvasserName(edeCanvasserName.getText().toString())
                .sessionStatus(DETAILS_ENTERED);

        if (updateTimeout) {
            builder.sessionTimeout(timeout);
        }

        db.update(Session.TABLE,
                builder.build(),
                Session._ID + " = ? ", String.valueOf(currentSessionRowId.get()));

        listener.setState(DETAILS_ENTERED, true);

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
}
