package com.rockthevote.grommet.ui.eventFlow;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Mechanical Man, LLC on 7/17/17. Grommet
 */

public class EventDetailFlowAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<EventFlowPage> pages = new ArrayList<>();

    public EventDetailFlowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View view;
        switch (position) {
            case 0:
                view = new EventDetailsEditable(context);
                break;
            case 1:
                view = new SessionTimeTracking(context);
                break;
            case 2:
                view = new SessionSummary(context);
                break;
            default:
                view = new EventDetailsEditable(context);
                break;
        }

        collection.addView(view);
        pages.add((EventFlowPage) view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
        pages.remove((EventFlowPage) view);
    }

    public EventFlowPage getPageAtPosition(int position) {
        return pages.get(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
