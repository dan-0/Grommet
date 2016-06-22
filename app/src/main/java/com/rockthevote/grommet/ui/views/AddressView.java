package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rockthevote.grommet.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddressView extends FrameLayout {

    @BindView(R.id.edittext_street) EditText street;
    @BindView(R.id.edittext_zip_code) EditText zipCode;
    @BindView(R.id.edittext_city) EditText city;
    @BindView(R.id.edittext_state) EditText state;
    @BindView(R.id.edittext_unit) EditText unit;
    @BindView(R.id.address_section_title) TextView sectionTitle;


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
        }
    }

    @Nullable
    public String getStreetAddress() {
        return street.getText().toString();
    }

    @Nullable
    public String getZip() {
        return zipCode.getText().toString();
    }

    @Nullable
    public String getCity() {
        return city.getText().toString();
    }

    @Nullable
    public String getState() {
        return state.getText().toString();
    }

    @Nullable
    public String getUnit() {
        return unit.getText().toString();
    }

}
