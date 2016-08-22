package com.rockthevote.grommet.ui.views;


import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.api.model.PartnerNameResponse;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventRegTotal;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.ui.misc.BetterViewAnimator;
import com.rockthevote.grommet.util.Strings;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EventDetails extends FrameLayout {

    static final ButterKnife.Setter<View, Boolean> ENABLED =
            (view, value, index) -> view.setEnabled(value);

    @BindViews({R.id.ede_til_canvasser_name, R.id.ede_til_event_name,
                       R.id.ede_til_event_zip, R.id.ede_til_partner_id})
    List<TextInputLayout> editableViews;

    @BindView(R.id.ed_animator) BetterViewAnimator viewAnimator;

    @BindView(R.id.ed_canvasser_name) TextView edCanvasserName;

    @BindView(R.id.ed_event_name) TextView edEventName;

    @BindView(R.id.ed_event_zip) TextView edEventZip;

    @BindView(R.id.ed_partner_name) TextView edPartnerName;

    @BindView(R.id.ede_canvasser_name) EditText edeCanvasserName;

    @BindView(R.id.ede_event_name) EditText edeEventName;

    @BindView(R.id.ede_event_zip) EditText edeEventZip;

    @BindView(R.id.ede_til_partner_id) TextInputLayout edePartnerIdTIL;

    @BindView(R.id.ede_partner_id) EditText edePartnerId;

    @Inject @EventRegTotal Preference<Integer> eventRegTotalPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;

    @Inject @EventName Preference<String> eventNamePref;

    @Inject @EventZip Preference<String> eventZipPref;

    @Inject @PartnerId Preference<String> partnerIdPref;

    @Inject @PartnerName Preference<String> partnerNamePref;

    @Inject RockyService rockyService;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    private EditableActionView.EditableActionViewListener listener;

    private EditableActionView editableActionView;

    public EventDetails(Context context) {
        this(context, null);
    }

    public EventDetails(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventDetails(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_details, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
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

    public void setEditableActionView(EditableActionView view) {
        editableActionView = view;
        listener = new EditableActionView.EditableActionViewListener() {
            @Override
            public void onEdit() {
                enableEditMode(true);
            }

            @Override
            public void onCancel() {
                enableEditMode(false);
            }

            @Override
            public void onSave() {

                // allow the user to not set a partner ID
                if (Strings.isBlank(edePartnerId.getText().toString())) {
                    setPartnerName("");
                } else if (edePartnerId.getText().toString().equals(partnerIdPref.get())) {
                    setPartnerName(partnerNamePref.get());
                } else {
                    rockyService.getPartnerName(edePartnerId.getText().toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(() -> {
                                editableActionView.showSpinner();
                                ButterKnife.apply(editableViews, ENABLED, false);
                            })
                            .doOnCompleted(() -> ButterKnife.apply(editableViews, ENABLED, true))
                            .subscribe(result -> {
                                if (!result.isError() && result.response().isSuccessful()) {
                                    PartnerNameResponse partnerNameResponse = result.response().body();
                                    if (partnerNameResponse.isValid()) {
                                        setPartnerName(partnerNameResponse.partnerName());
                                    } else {
                                        edePartnerIdTIL.setError(
                                                getContext().getString(R.string.error_partner_id));
                                        editableActionView.showSaveCancel();
                                    }
                                } else {
                                    edePartnerIdTIL.setError(
                                            getContext().getString(R.string.error_partner_id));
                                    editableActionView.showSaveCancel();
                                }
                            });
                }
            }
        };

        editableActionView.setListener(listener);
    }

    private void setPartnerName(String name) {
        canvasserNamePref.set(edeCanvasserName.getText().toString());
        eventNamePref.set(edeEventName.getText().toString());
        eventZipPref.set(edeEventZip.getText().toString());
        partnerIdPref.set(edePartnerId.getText().toString());
        partnerNamePref.set(name);

        // reset event registration total when event name changes
        if (!edeEventName.getText().toString().equals(eventNamePref.get())) {
            eventRegTotalPref.set(0);
        }
        enableEditMode(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            subscriptions.unsubscribe();
        }
    }

    public void enableEditMode(boolean enable) {

        viewAnimator.setDisplayedChildId(enable ? R.id.editable_details : R.id.static_details);

        if (enable) {
            editableActionView.showSaveCancel();
            edeCanvasserName.setText(canvasserNamePref.get());
            edeEventName.setText(eventNamePref.get());
            edeEventZip.setText(eventZipPref.get());
            edePartnerId.setText(partnerIdPref.get());
        } else {
            editableActionView.showEditButton();
            // close keyboard if it's showing
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }

    }

    public boolean isEditable() {
        return viewAnimator.getDisplayedChildId() == R.id.editable_details;
    }
}
