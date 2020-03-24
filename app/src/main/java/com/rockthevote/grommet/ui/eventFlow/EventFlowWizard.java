package com.rockthevote.grommet.ui.eventFlow;

import android.content.Context;
import android.os.Handler;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Session.SessionStatus;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rockthevote.grommet.data.db.model.Session.SessionStatus.DETAILS_ENTERED;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public class EventFlowWizard extends FrameLayout implements EventFlowCallback {

    @BindView(R.id.viewpager) ViewPager viewPager;

    private EventDetailFlowAdapter adapter;

    public EventFlowWizard(Context context) {
        this(context, null);
    }

    public EventFlowWizard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventFlowWizard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_flow_wizard, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);

            adapter = new EventDetailFlowAdapter(getContext());
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(3);

            //disable scrolling on the view pager
            viewPager.setOnTouchListener((v, event) -> true);

            new Handler().post(() -> setState(DETAILS_ENTERED, false));
        }
    }


    @Override
    public void setState(SessionStatus status, boolean smoothScroll) {

        // unregister the current page from callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .unregisterCallbackListener();

        switch (status) {
            case PARTNER_UPDATE:
                viewPager.setCurrentItem(0, smoothScroll);
                break;
            case SESSION_CLEARED:
                // dummy state to let us clear entry fields in the editable page
                viewPager.setCurrentItem(1, smoothScroll);
                ((EventDetailsEditable) adapter.getPageAtPosition(1)).resetForm();
                break;
            case NEW_SESSION:
                // show the event details editable screen
                viewPager.setCurrentItem(1, smoothScroll);
                break;
            case DETAILS_ENTERED: // fall through
            case CLOCKED_IN:
                // show the event details static page along with the clock-out option
                // add location data
                viewPager.setCurrentItem(2, smoothScroll);
                break;
            case CLOCKED_OUT:
                // show session summary
                viewPager.setCurrentItem(3, smoothScroll);
                ((SessionSummary) adapter.getPageAtPosition(3)).updateUI();
                break;
        }

        // register the current page for callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .registerCallbackListener(this);
    }


}
