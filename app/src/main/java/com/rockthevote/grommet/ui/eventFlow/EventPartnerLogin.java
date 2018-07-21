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

import com.f2prateek.rx.preferences2.Preference;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.data.prefs.PartnerTimeout;
import com.rockthevote.grommet.data.prefs.RegistrationDeadline;
import com.rockthevote.grommet.data.prefs.RegistrationText;
import com.rockthevote.grommet.ui.misc.BetterViewAnimator;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.NEW_SESSION;

/**
 * Created by Mechanical Man on 7/14/18.
 */
public class EventPartnerLogin extends FrameLayout implements EventFlowPage {

    @Inject RockyService rockyService;
    @Inject BriteDatabase db;

    // preferences
    @Inject @PartnerId Preference<String> partnerIdPref;
    @Inject @PartnerName Preference<String> partnerNamePref;
    @Inject @RegistrationDeadline Preference<Date> registrationDeadlinePref;
    @Inject @RegistrationText Preference<RegistrationNotificationText> registrationTextPref;
    @Inject @PartnerTimeout Preference<Long> partnerTimeoutPref;

    @NotEmpty
    @BindView(R.id.ede_til_partner_id) TextInputLayout edePartnerIdTIL;
    @BindView(R.id.ede_partner_id) EditText edePartnerId;

    @BindView(R.id.save_view_animator) BetterViewAnimator viewAnimator;

    private ObservableValidator validator;

    private EventFlowCallback listener;

    public EventPartnerLogin(Context context) {
        this(context, null);
    }

    public EventPartnerLogin(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventPartnerLogin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_partner_login, this);

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
            edePartnerId.setText(partnerIdPref.get());
        }
    }

    @OnClick(R.id.event_partner_id_save)
    public void onClickSave(View v) {

        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // allow the user to not set a partner ID
        if (validator.validate().toBlocking().single()) {
            if (edePartnerId.getText().toString().equals(partnerIdPref.get())) {
                updateSessionData(false, null, 0,
                        null, null);
            } else {
                rockyService.getPartnerName(edePartnerId.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .delay(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(() -> {
                            viewAnimator.setDisplayedChildId(R.id.save_progress_bar);
                            edePartnerId.setEnabled(false);
                        })
                        .doOnCompleted(() -> {
                            viewAnimator.setDisplayedChildId(R.id.event_partner_id_save);
                            edePartnerId.setEnabled(true);
                        })
                        .subscribe(result -> {
                            if (!result.isError() && result.response().isSuccessful()) {
                                PartnerNameResponse partnerNameResponse = result.response().body();
                                if (partnerNameResponse.isValid()) {
                                    updateSessionData(true,
                                            partnerNameResponse.partnerName(),
                                            partnerNameResponse.sessionTimeoutLength(),
                                            partnerNameResponse.registrationNotificationText(),
                                            partnerNameResponse.registrationDeadlineDate());
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

    @OnClick(R.id.clear_partner_info)
    public void onClickClearPartnerInfo(View v) {
        partnerIdPref.set("");
        edePartnerId.setText(partnerIdPref.get());
    }

    private void updateSessionData(boolean updatePartnerInfo, String partnerName, long timeout,
                                   RegistrationNotificationText registrationText,
                                   Date registrationDeadline) {

        if (updatePartnerInfo) {
            // update preferences
            partnerIdPref.set(edePartnerId.getText().toString());
            partnerNamePref.set(partnerName);
            registrationTextPref.set(registrationText);
            registrationDeadlinePref.set(registrationDeadline);
            partnerTimeoutPref.set(timeout);
        }

        // update the event flow listener
        listener.setState(NEW_SESSION, true);
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
