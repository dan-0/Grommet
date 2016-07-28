package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.ChildrenViewStateHelper;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.rockthevote.grommet.data.db.Db.DEBOUNCE;

public class AddressView extends FrameLayout {

    @NotEmpty
    @BindView(R.id.til_street_address) TextInputLayout streetTIL;
    @BindView(R.id.street) EditText streetEditText;

    @BindView(R.id.unit) EditText unitEditText;
    @BindView(R.id.spinner_county) Spinner counties;

    @NotEmpty
    @BindView(R.id.til_city) TextInputLayout cityTIL;
    @BindView(R.id.city) EditText cityEditText;

    @BindView(R.id.spinner_state) Spinner states;

    @NotEmpty
    @BindView(R.id.til_zip_code) TextInputLayout zipTIL;
    @BindView(R.id.zip) EditText zipEditText;

    @BindView(R.id.address_section_title) TextView sectionTitle;

    @Inject
    @CurrentRockyRequestId
    Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    ObservableValidator validator;

    private ArrayAdapter<CharSequence> countyAdapter;

    private ArrayAdapter<CharSequence> stateAdapter;

    private Address.Type type;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    public AddressView(Context context) {
        this(context, null);
    }

    public AddressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_address, this);

        if (!isInEditMode()) {

            Injector.obtain(context).inject(this);
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.AddressView,
                    0, 0);

            try {
                int val = typedArray.getInt(R.styleable.AddressView_address_type, 0);
                switch (val) {
                    case 1:
                        type = Address.Type.MAILING;
                        break;
                    case 2:
                        type = Address.Type.PREVIOUS;
                        break;
                    case 3:
                        type = Address.Type.REGISTRATION;
                        break;
                }
            } finally {
                typedArray.recycle();
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        validator = new ObservableValidator(this, getContext());

        switch (type) {
            case REGISTRATION:
                sectionTitle.setText(R.string.section_label_registration_address);
                break;
            case MAILING:
                sectionTitle.setText(R.string.section_label_mailing_address);
                break;
            case PREVIOUS:
                sectionTitle.setText(R.string.section_label_previous_address);
                break;
        }

        countyAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.pa_counties, android.R.layout.simple_spinner_item);
        countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        counties.setAdapter(countyAdapter);

        stateAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.states, android.R.layout.simple_spinner_item);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        states.setAdapter(stateAdapter);
        states.setSelection(stateAdapter.getPosition("PA"));

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subscriptions.add(Observable.combineLatest(RxTextView.afterTextChangeEvents(streetEditText),
                RxTextView.afterTextChangeEvents(unitEditText),
                RxTextView.afterTextChangeEvents(cityEditText),
                RxAdapterView.itemSelections(states),
                RxTextView.afterTextChangeEvents(zipEditText),
                RxAdapterView.itemSelections(counties),
                (street, unit, city, statePos, zip, countyPos) -> new Address.Builder()
                        .streetName(street.editable().toString())
                        .subAddress(unit.editable().toString())
                        .municipalJurisdiction(city.editable().toString())
                        .state(stateAdapter.getItem(statePos).toString())
                        .zip(zip.editable().toString())
                        .county(countyAdapter.getItem(countyPos).toString())
                        .build())
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .subscribe(contentValues -> {
                    Address.insertOrUpdate(db, rockyRequestRowId.get(), type, contentValues);
                }));

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscriptions.unsubscribe();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable("superState", super.onSaveInstanceState());
        state.putSparseParcelableArray(ChildrenViewStateHelper.DEFAULT_CHILDREN_STATE_KEY,
                ChildrenViewStateHelper.newInstance(this).saveChildrenState());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle localState = (Bundle) state;
            super.onRestoreInstanceState(localState.getParcelable("superState"));
            ChildrenViewStateHelper.newInstance(this).restoreChildrenState(localState
                    .getSparseParcelableArray(ChildrenViewStateHelper.DEFAULT_CHILDREN_STATE_KEY));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    public Observable<Boolean> verify() {
        return validator.validate();
    }
}