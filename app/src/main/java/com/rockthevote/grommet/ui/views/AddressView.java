package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Address;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.ChildrenViewStateHelper;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.util.Strings;
import com.rockthevote.grommet.util.ZipTextWatcher;
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
    private static final String PA_ABREV = "PA";

    private static final String COUNTY_ENABLED_KEY = "county_enabled_key";

    private String childrenStateKey;
    private String superStateKey;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_street_address) TextInputLayout streetTIL;
    @BindView(R.id.street) EditText streetEditText;

    @BindView(R.id.til_street_address_2) TextInputLayout streetTIL2;
    @BindView(R.id.street_2) EditText streetEditText2;

    @BindView(R.id.til_unit) TextInputLayout unitTIL;
    @BindView(R.id.unit) EditText unitEditText;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.spinner_county) BetterSpinner countySpinner;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_city) TextInputLayout cityTIL;
    @BindView(R.id.city) EditText cityEditText;

    @BindView(R.id.spinner_state) BetterSpinner stateSpinner;
    @BindView(R.id.spinner_unit_type) BetterSpinner unitTypeSpinner;

    @Pattern(regex = "^[0-9]{5}(?:-[0-9]{4})?$", messageResId = R.string.zip_code_error)
    @BindView(R.id.til_zip_code) TextInputLayout zipTIL;
    @BindView(R.id.zip) EditText zipEditText;

    @BindView(R.id.address_section_title) TextView sectionTitle;

    @Inject
    @CurrentRockyRequestId
    Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private ObservableValidator validator;

    private ArrayAdapter<CharSequence> countyAdapter;
    private ArrayAdapter<CharSequence> stateAdapter;
    private ArrayAdapter<CharSequence> unitTypeAdapter;

    private Address.Type type;
    private CompositeSubscription subscriptions;
    private ZipTextWatcher zipTextWatcher = new ZipTextWatcher();

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
                        type = Address.Type.MAILING_ADDRESS;
                        break;
                    case 2:
                        type = Address.Type.PREVIOUS_ADDRESS;
                        break;
                    case 3:
                        type = Address.Type.REGISTRATION_ADDRESS;
                        break;
                    case 4:
                        type = Address.Type.ASSISTANT_ADDRESS;
                        break;
                }
            } finally {
                typedArray.recycle();
            }

            superStateKey = AddressView.class.getSimpleName() + ".superState." + type.toString();
            childrenStateKey = AddressView.class.getSimpleName() + ".childState." + type.toString();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
            validator = new ObservableValidator(this, getContext());

            switch (type) {
                case REGISTRATION_ADDRESS:
                    sectionTitle.setText(R.string.section_label_registration_address);
                    stateSpinner.setEnabled(false);
                    break;
                case MAILING_ADDRESS:
                    sectionTitle.setText(R.string.section_label_mailing_address);
                    break;
                case PREVIOUS_ADDRESS:
                    sectionTitle.setText(R.string.section_label_previous_address);
                    break;
                case ASSISTANT_ADDRESS:
                    sectionTitle.setText(R.string.section_label_registration_address);
            }

            countyAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.pa_counties, android.R.layout.simple_list_item_1);

            countySpinner.setAdapter(countyAdapter);
            countySpinner.setHeight((int) getResources().getDimension(R.dimen.list_pop_up_max_height));
            countySpinner.setOnItemClickListener((adapterView, view, i, l) -> {
                countySpinner.getEditText().setText(countyAdapter.getItem(i));
                countySpinner.dismiss();
            });

            stateAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.states, android.R.layout.simple_list_item_1);

            stateSpinner.setAdapter(stateAdapter);
            stateSpinner.setHeight((int) getResources().getDimension(R.dimen.list_pop_up_max_height));
            stateSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
                stateSpinner.getEditText().setText(stateAdapter.getItem(i));

                if (!PA_ABREV.equals(stateAdapter.getItem(i))) {
                    countySpinner.setErrorEnabled(false);
                    countySpinner.setEnabled(false);
                } else {
                    countySpinner.setEnabled(true);
                }

                stateSpinner.dismiss();
            });
            if (Strings.isBlank(stateSpinner.getEditText().getEditableText().toString())) {
                stateSpinner.getEditText().setText(stateAdapter.getItem(stateAdapter.getPosition(PA_ABREV)));
            }

            unitTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.unit_types, android.R.layout.simple_list_item_1);
            unitTypeSpinner.setAdapter(unitTypeAdapter);
            unitTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                unitTypeSpinner.getEditText().setText(unitTypeAdapter.getItem(position));
                unitTypeSpinner.dismiss();
            });

        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            zipEditText.addTextChangedListener(zipTextWatcher);

            subscriptions = new CompositeSubscription();
            subscriptions.add(Observable.combineLatest(
                    RxTextView.afterTextChangeEvents(streetEditText),
                    RxTextView.afterTextChangeEvents(streetEditText2),
                    RxTextView.afterTextChangeEvents(unitEditText),
                    RxTextView.afterTextChangeEvents(cityEditText),
                    RxTextView.afterTextChangeEvents(stateSpinner.getEditText()),
                    RxTextView.afterTextChangeEvents(zipEditText),
                    RxTextView.afterTextChangeEvents(countySpinner.getEditText()),
                    RxTextView.afterTextChangeEvents(unitTypeSpinner.getEditText()),
                    (street, street2, unit, city, state, zip, county, unitType) -> new Address.Builder()
                            .streetName(street.editable().toString())
                            .streetName2(street2.editable().toString())
                            .subAddress(unit.editable().toString())
                            .municipalJurisdiction(city.editable().toString())
                            .state(state.editable().toString())
                            .zip(zip.editable().toString())
                            .county(county.editable().toString())
                            .subAddressType(unitType.editable().toString())
                            .build())
                    .observeOn(Schedulers.io())
                    .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                    .subscribe(contentValues -> {
                        Address.insertOrUpdate(db, rockyRequestRowId.get(), type, contentValues);
                    }));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscriptions.unsubscribe();
        zipEditText.removeTextChangedListener(zipTextWatcher);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable(superStateKey, super.onSaveInstanceState());
        state.putBoolean(COUNTY_ENABLED_KEY, countySpinner.isEnabled());
        state.putSparseParcelableArray(childrenStateKey,
                ChildrenViewStateHelper.newInstance(this).saveChildrenState(childrenStateKey));
        return state;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle localState = (Bundle) state;
            super.onRestoreInstanceState(localState.getParcelable(superStateKey));
            ChildrenViewStateHelper.newInstance(this).restoreChildrenState(localState
                    .getSparseParcelableArray(childrenStateKey), childrenStateKey);
            countySpinner.setEnabled(localState.getBoolean(COUNTY_ENABLED_KEY, true));
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

        unitTypeSpinner.setError(null);
        unitTIL.setError(null);

        String unit = unitEditText.getText().toString();
        String type = unitTypeSpinner.getEditText().getText().toString();

        boolean typeValidation = (Strings.isBlank(unit) || !Strings.isBlank(type));
        if (!typeValidation) {
            unitTypeSpinner.setError(getContext().getString(R.string.required_field));
        }

        boolean unitValidation = (!Strings.isBlank(unit) || Strings.isBlank(type));
        if (!unitValidation) {
            unitTIL.setError(getContext().getString(R.string.required_field));
        }

        return validator.validate()
                .map(valid -> valid && (typeValidation && unitValidation));
    }
}
