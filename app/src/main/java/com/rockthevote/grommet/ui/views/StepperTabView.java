package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.ui.misc.ChildrenViewStateHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepperTabView extends FrameLayout {

    @BindView(R.id.step_number) TextView stepNumber;
    @BindView(R.id.step_name) TextView stepName;

    public StepperTabView(Context context) {
        this(context, null);
    }

    public StepperTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepperTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.tab_stepper, this);

        if (!isInEditMode()) {
            ButterKnife.bind(this);
        }

    }

    public StepperTabView setStepNumber(int num) {
        stepNumber.setText(String.valueOf(num));
        return this;
    }

    public StepperTabView setStepName(String name) {
        stepName.setText(name);
        return this;
    }

    @Override
    public void setEnabled(boolean enabled) {
        stepNumber.setEnabled(enabled);
        stepName.setEnabled(enabled);
        super.setEnabled(enabled);
    }

}
