package com.rockthevote.grommet.ui.registration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.ui.BaseActivity;
import com.rockthevote.grommet.ui.ViewContainer;
import com.rockthevote.grommet.ui.misc.StepperTabLayout;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.ABANDONED;


public class RegistrationActivity extends BaseActivity {
    @Inject ViewContainer viewContainer;
    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;
    @Inject BriteDatabase db;


    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.tabLayout) StepperTabLayout tabLayout;
    @BindView(R.id.button_previous) Button previousButton;
    @BindView(R.id.button_next) Button nextButton;

    private RegistrationPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup contentView = getContentView();
        getLayoutInflater().inflate(R.layout.activity_registration, contentView);
        ButterKnife.bind(this, contentView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager();
    }

    private void setupViewPager() {
        adapter = new RegistrationPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.setupWithViewPager(viewPager);

        // Steppers are sequential and we want to prevent skipping steps
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(false);
        }

        updateNavigation(viewPager.getCurrentItem());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int prevPage = viewPager.getCurrentItem();

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                updateNavigation(position);

                //only check if we're swiping right
                if (position - 1 == prevPage) {
                    ((BaseRegistrationFragment) adapter.getItem(prevPage))
                            .verify()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(isValid -> {
                                if (isValid) {
                                    tabLayout.enableTabAtPosition(position);
                                    prevPage = position;
                                } else {
                                    viewPager.setCurrentItem(prevPage);
                                }
                            });
                } else {
                    prevPage = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    private void updateNavigation(int pagePosition){
        previousButton.setEnabled(pagePosition != 0);
        nextButton.setEnabled(pagePosition != adapter.getCount() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                showCancelDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showCancelDialog();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_dialog_message)
                .setPositiveButton(R.string.dialog_yes, ((dialog, i) -> {
                    db.update(
                            RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .status(ABANDONED)
                                    .build(),
                            RockyRequest._ID + " = ? ",
                            String.valueOf(rockyRequestRowId.get()));

                    finish();

                }))
                .setNegativeButton(R.string.dialog_no, ((dialog, i) -> dialog.dismiss()))
                .create()
                .show();
    }

    @OnClick(R.id.button_previous)
    public void onPreviousClick(View v) {
        int curPage = viewPager.getCurrentItem();
        if (curPage > 0) {
            viewPager.setCurrentItem(curPage - 1);
        }
    }

    @OnClick(R.id.button_next)
    public void onNextClick(View v) {
        int curPage = viewPager.getCurrentItem();
        if (curPage < adapter.getCount() - 1) {
            ((BaseRegistrationFragment) adapter.getItem(curPage))
                    .verify()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isValid -> {
                        if (isValid) {
                            tabLayout.enableTabAtPosition(curPage + 1);
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                        }
                    });
        }
    }

}
