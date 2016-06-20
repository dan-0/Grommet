package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.api.model.Suffix;
import com.rockthevote.grommet.data.api.model.Title;
import com.rockthevote.grommet.ui.misc.EnumAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NameView extends FrameLayout {
    @BindView(R.id.name_section_title) TextView sectionTitle;
    @BindView(R.id.spinner_title) Spinner spinnerTitle;
    @BindView(R.id.spinner_suffix) Spinner spinnerSuffix;

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
            ButterKnife.bind(this);

            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.GrommetView,
                    0, 0);

            try {
                sectionTitle.setText(typedArray.getResourceId(R.styleable.GrommetView_section_title, 0));
            } finally {
                typedArray.recycle();
            }

            setupSpinners();
        }
    }


    private void setupSpinners() {
        final EnumAdapter<Title> titleEnumAdapter = new EnumAdapter<>(getContext(), Title.class);
        spinnerTitle.setAdapter(titleEnumAdapter);

        final EnumAdapter<Suffix> suffixEnumAdapter = new EnumAdapter<>(getContext(), Suffix.class);
        spinnerSuffix.setAdapter(suffixEnumAdapter);
    }

}
