package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.textfield.TextInputLayout;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.NameLegacy;
import com.rockthevote.grommet.data.db.model.Prefix;
import com.rockthevote.grommet.data.db.model.Suffix;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.ChildrenViewStateHelper;
import com.rockthevote.grommet.ui.misc.EnumAdapter;
import com.rockthevote.grommet.ui.misc.ObservableValidator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class NameView extends GridLayout {

    private String childrenStateKey;
    private String superStateKey;

    @BindView(R.id.name_section_title) TextView sectionTitle;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.spinner_title) BetterSpinner titleSpinner;

    @BindView(R.id.spinner_suffix) BetterSpinner suffixSpinner;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_first_name) TextInputLayout firstNameTIL;
    @BindView(R.id.first_name) EditText firstNameEditText;

    @BindView(R.id.middle_name) EditText middleNameEditText;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_last_name) TextInputLayout lastNameTIL;
    @BindView(R.id.last_name) EditText lastNameEditText;

    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;

    private ObservableValidator validator;

    private EnumAdapter<Prefix> titleEnumAdapter;

    private EnumAdapter<Suffix> suffixEnumAdapter;

    private NameLegacy.Type type;

    private CompositeSubscription subscriptions;

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
                        type = NameLegacy.Type.CURRENT_NAME;
                        break;
                    case 2:
                        type = NameLegacy.Type.PREVIOUS_NAME;
                        break;
                    case 3:
                        type = NameLegacy.Type.ASSISTANT_NAME;
                        break;
                }
            } finally {
                typedArray.recycle();
            }
            superStateKey = NameView.class.getSimpleName() + ".superState." + type.toString();
            childrenStateKey = NameView.class.getSimpleName() + ".childState." + type.toString();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ButterKnife.bind(this);

            validator = new ObservableValidator(this, getContext());

            switch (type) {
                case CURRENT_NAME:
                    sectionTitle.setText(R.string.section_label_name);
                    break;
                case PREVIOUS_NAME:
                    sectionTitle.setText(R.string.section_label_previous_name);
                    break;
                case ASSISTANT_NAME:
                    sectionTitle.setText(R.string.section_label_name);
                    break;
            }

            titleEnumAdapter = new EnumAdapter<>(getContext(), Prefix.class);
            titleSpinner.setAdapter(titleEnumAdapter);
            titleSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
                titleSpinner.getEditText().setText(titleEnumAdapter.getItem(i).toString());
                titleSpinner.dismiss();
            });

            suffixEnumAdapter = new EnumAdapter<>(getContext(), Suffix.class);
            suffixSpinner.setAdapter(suffixEnumAdapter);
            suffixSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
                suffixSpinner.getEditText().setText(suffixEnumAdapter.getItem(i).toString());
                suffixSpinner.dismiss();
            });
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            subscriptions = new CompositeSubscription();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscriptions.unsubscribe();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable(superStateKey, super.onSaveInstanceState());
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
