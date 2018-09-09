package com.rockthevote.grommet.ui.registration;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.Name;
import com.rockthevote.grommet.data.db.model.RockyRequest;
import com.rockthevote.grommet.data.db.model.Session;
import com.rockthevote.grommet.data.prefs.CurrentRockyRequestId;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.ui.BaseActivity;
import com.rockthevote.grommet.ui.ViewContainer;
import com.rockthevote.grommet.ui.misc.StepperTabLayout;
import com.rockthevote.grommet.util.KeyboardUtil;
import com.rockthevote.grommet.util.LocaleUtils;
import com.rockthevote.grommet.util.Strings;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static android.support.v4.view.ViewPager.OnPageChangeListener;
import static com.rockthevote.grommet.data.db.model.RockyRequest.Status.ABANDONED;


public class RegistrationActivity extends BaseActivity {

    @Inject ViewContainer viewContainer;

    @Inject BriteDatabase db;
    @Inject @CurrentRockyRequestId Preference<Long> rockyRequestRowId;
    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    @BindView(R.id.appbar) AppBarLayout appbar;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.tabLayout) StepperTabLayout tabLayout;
    @BindView(R.id.button_previous) Button previousButton;
    @BindView(R.id.button_next) Button nextButton;

    private RegistrationPagerAdapter adapter;

    private OnPageChangeListener listener;

    private KeyboardUtil keyboardUtil;

    public RegistrationActivity() {
        LocaleUtils.updateConfig(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Slide());
        getWindow().setEnterTransition(new Slide());

        super.onCreate(savedInstanceState);
        Injector.obtain(this).inject(this);

        ViewGroup contentView = getContentView();
        getLayoutInflater().inflate(R.layout.activity_registration, contentView);
        ButterKnife.bind(this, contentView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupViewPager();
        keyboardUtil = new KeyboardUtil(this, appbar);
    }

    private void setupViewPager() {
        adapter = new RegistrationPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        tabLayout.setupWithViewPager(viewPager);

        // Steppers are sequential and we want to prevent skipping steps
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listener = new RegistrationPageListener();
        viewPager.addOnPageChangeListener(listener);
        updateNavigation(viewPager.getCurrentItem());
        keyboardUtil.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewPager.removeOnPageChangeListener(listener);
        keyboardUtil.disable();
    }

    private void updateNavigation(int pagePosition) {
        previousButton.setEnabled(pagePosition != 0);
        nextButton.setEnabled(pagePosition != adapter.getCount() - 1);
        KeyboardUtil.hideKeyboard(this);
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
            case R.id.action_english:
                if (!Locale.getDefault().equals(new Locale("en"))) {
                    LocaleUtils.setLocale(new Locale("en"));
                    recreate();
                }
                return true;
            case R.id.action_espanol:
                if (!Locale.getDefault().equals(new Locale("es"))) {
                    LocaleUtils.setLocale(new Locale("es"));
                    recreate();
                }
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
                    // set the application to abandoned so it gets cleaned up
                    db.update(
                            RockyRequest.TABLE,
                            new RockyRequest.Builder()
                                    .status(ABANDONED)
                                    .build(),
                            RockyRequest._ID + " = ? ",
                            String.valueOf(rockyRequestRowId.get()));

                    // update abandoned count for the session, only if the voter entered some data
                    Cursor nameCursor = db.query(Name.SELECT_BY_TYPE,
                            String.valueOf(rockyRequestRowId.get()),
                            Name.Type.CURRENT_NAME.toString());

                    if (nameCursor.moveToNext()) {
                        Name name = Name.MAPPER.call(nameCursor);
                        if (!Strings.isBlank(name.firstName())) {
                            nameCursor.close();
                            Cursor sessionCursor =
                                    db.query(Session.SELECT_CURRENT_SESSION);

                            if (sessionCursor.moveToNext()) {
                                Session session = Session.MAPPER.call(sessionCursor);
                                db.update(Session.TABLE,
                                        new Session.Builder()
                                                .totalAbandond(session.totalAbandoned() + 1)
                                                .build(), Session._ID + " = ? ",
                                        String.valueOf(currentSessionRowId.get()));
                            }
                            sessionCursor.close();
                        }
                    }

                    LocaleUtils.setLocale(new Locale("en"));
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

    private class RegistrationPageListener implements OnPageChangeListener {
        private int prevPage = viewPager.getCurrentItem();

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
    }
}
