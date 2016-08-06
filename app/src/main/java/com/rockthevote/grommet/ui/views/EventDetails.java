package com.rockthevote.grommet.ui.views;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventRegTotal;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.ui.misc.BetterViewAnimator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class EventDetails extends FrameLayout {
    private static final String SAVE = "save";

    @BindView(R.id.ed_canvasser_name) TextView edCanvasserName;

    @BindView(R.id.ed_event_name) TextView edEventName;

    @BindView(R.id.ed_event_zip) TextView edEventZip;

    @BindView(R.id.ed_partner_id) TextView edPartnerId;

    @BindView(R.id.ede_canvasser_name) TextView edeCanvasserName;

    @BindView(R.id.ede_event_name) TextView edeEventName;

    @BindView(R.id.ede_event_zip) TextView edeEventZip;

    @BindView(R.id.ede_partner_id) TextView edePartnerId;

    @BindView(R.id.ed_animator) BetterViewAnimator viewAnimator;

    @Inject @EventRegTotal Preference<Integer> eventRegTotalPref;

    @Inject @CanvasserName Preference<String> canvasserNamePref;

    @Inject @EventName Preference<String> eventNamePref;

    @Inject @EventZip Preference<String> eventZipPref;

    @Inject @PartnerId Preference<String> partnerIdPref;

    private PublishSubject<String> publishSubject = PublishSubject.create();

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

            subscriptions.add(partnerIdPref.asObservable()
                    .subscribe(name -> edPartnerId.setText(name)));


            //reset event registration total when event name changes
            subscriptions.add(Observable.combineLatest(
                    RxTextView.textChanges(edeEventName), publishSubject,
                    (eventName, ignore) -> "")
                    .subscribe(s -> {
                        eventRegTotalPref.set(0);
                    })
            );
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
                canvasserNamePref.set(edeCanvasserName.getText().toString());
                eventNamePref.set(edeEventName.getText().toString());
                eventZipPref.set(edeEventZip.getText().toString());
                partnerIdPref.set(edePartnerId.getText().toString());

                publishSubject.onNext(SAVE);
                enableEditMode(false);
            }
        };

        editableActionView.setListener(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            subscriptions.unsubscribe();
        }
    }

    public void enableEditMode(boolean enable) {
        editableActionView.enableEditMode(enable);
        viewAnimator.setDisplayedChildId(enable ? R.id.editable_details : R.id.static_details);

        if (enable) {
            edeCanvasserName.setText(canvasserNamePref.get());
            edeEventName.setText(eventNamePref.get());
            edeEventZip.setText(eventZipPref.get());
            edePartnerId.setText(partnerIdPref.get());
        } else {
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
