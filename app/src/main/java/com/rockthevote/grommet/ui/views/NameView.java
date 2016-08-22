package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.ChildrenViewStateHelper;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
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
import static com.rockthevote.grommet.data.db.model.Name.Prefix;
import static com.rockthevote.grommet.data.db.model.Name.Suffix;

public class NameView extends FrameLayout {

    @BindView(R.id.name_section_title) TextView sectionTitle;

    @NotEmpty
    @BindView(R.id.spinner_title) BetterSpinner titleSpinner;

    @BindView(R.id.spinner_suffix) BetterSpinner suffixSpinner;

    @NotEmpty
    @BindView(R.id.til_first_name) TextInputLayout firstNameTIL;
    @BindView(R.id.first_name) EditText firstNameEditText;

    @BindView(R.id.middle_name) EditText middleNameEditText;

    @NotEmpty
    @BindView(R.id.til_last_name) TextInputLayout lastNameTIL;
    @BindView(R.id.last_name) EditText lastNameEditText;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    @Inject BriteDatabase db;

    private ObservableValidator validator;

    private EnumAdapter<Name.Prefix> titleEnumAdapter;

    private EnumAdapter<Name.Suffix> suffixEnumAdapter;

    private Name.Type type;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    public NameView(Context context) {
        this(context, null);
    }

    public NameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NameView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_name, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.NameView,
                    0, 0);

            try {
                int val = typedArray.getInt(R.styleable.NameView_name_type, 0);
                switch (val) {
                    case 1:
                        type = Name.Type.CURRENT_NAME;
                        break;
                    case 2:
                        type = Name.Type.PREVIOUS_NAME;
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
            case CURRENT_NAME:
                sectionTitle.setText(R.string.section_label_name);
                break;
            case PREVIOUS_NAME:
                sectionTitle.setText(R.string.section_label_previous_name);
                break;
        }

        titleEnumAdapter = new EnumAdapter<>(getContext(), Name.Prefix.class);
        titleSpinner.setAdapter(titleEnumAdapter);
        titleSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            titleSpinner.getEditText().setText(titleEnumAdapter.getItem(i).toString());
            titleSpinner.dismiss();
        });

        suffixEnumAdapter = new EnumAdapter<>(getContext(), Name.Suffix.class);
        suffixSpinner.setAdapter(suffixEnumAdapter);
        suffixSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
            suffixSpinner.getEditText().setText(suffixEnumAdapter.getItem(i).toString());
            suffixSpinner.dismiss();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        subscriptions.add(Observable.combineLatest(RxTextView.afterTextChangeEvents(firstNameEditText),
                RxTextView.afterTextChangeEvents(middleNameEditText),
                RxTextView.afterTextChangeEvents(lastNameEditText),
                RxTextView.afterTextChangeEvents(titleSpinner.getEditText()),
                RxTextView.afterTextChangeEvents(suffixSpinner.getEditText()),
                (firstName, middleName, lastName, title, suffix) -> new Name.Builder()
                        .firstName(firstName.editable().toString())
                        .middleName(middleName.editable().toString())
                        .lastName(lastName.editable().toString())
                        .prefix(Prefix.fromString(title.editable().toString()))
                        .suffix(Suffix.fromString(suffix.editable().toString()))
                        .build())
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .skip(1)
                .subscribe(contentValues -> {
                    Name.insertOrUpdate(db, rockyRequestRowId.get(), type, contentValues);
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
