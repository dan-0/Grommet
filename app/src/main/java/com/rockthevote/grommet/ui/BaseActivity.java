package com.rockthevote.grommet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.BuildConfig;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.EventName;

import javax.inject.Inject;

import dagger.ObjectGraph;


public class BaseActivity extends AppCompatActivity {
    private static final int NAVDRAWER_INVALID = -1;

    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 200;

    private DrawerLayout drawerLayout;
    private NavigationView drawer;
    private ViewGroup content;

    private ObjectGraph appGraph;
    private Handler handler;

    private ViewGroup container;
    @Inject ViewContainer viewContainer;


    @Inject
    @CanvasserName
    Preference<String> canvasserNamePref;

    @Inject
    @EventName
    Preference<String> eventNamePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();

        appGraph = Injector.obtain(getApplication());
        appGraph.inject(this);

        container = viewContainer.forActivity(this);

        if (getSelfNavDrawerItem() != NAVDRAWER_INVALID) {
            inflater.inflate(R.layout.activity_base, container);
            drawerLayout = (DrawerLayout) findViewById(R.id.base_drawer_layout);
            drawer = (NavigationView) findViewById(R.id.base_navigation);
            content = (ViewGroup) findViewById(R.id.base_content);
            handler = new Handler();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSelfNavDrawerItem() != NAVDRAWER_INVALID) {
            setUpNavDrawer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getSelfNavDrawerItem() != NAVDRAWER_INVALID) {
//            unsubscribe from subscriptions here
        }
    }

    private void setUpNavDrawer() {

        drawer.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == getSelfNavDrawerItem()) {
                drawerLayout.closeDrawers();
                return false;
            } else {
                handler.postDelayed(() -> goToNavDrawerItem(item), NAVDRAWER_LAUNCH_DELAY);
                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }

        });

        Toolbar toolbar = getToolbar();
        toolbar.setNavigationIcon(new DrawerArrowDrawable(this));
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        drawer.setCheckedItem(getSelfNavDrawerItem());

        TextView version = (TextView) drawer.getHeaderView(0).findViewById(R.id.version);
        version.setText("Version " + BuildConfig.VERSION_NAME);
    }

    private void goToNavDrawerItem(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_about:
                intent = new Intent(this, InfoActivity.class);
                intent.putExtra(InfoActivity.STRING_RES_PARAM, R.string.about_activity_text);
                break;
            case R.id.nav_data_usage:
                intent = new Intent(this, InfoActivity.class);
                intent.putExtra(InfoActivity.STRING_RES_PARAM, R.string.data_usage_activity_text);
                break;
            case R.id.nav_help:
                intent = new Intent(this, InfoActivity.class);
                intent.putExtra(InfoActivity.STRING_RES_PARAM, R.string.help_activity_text);
                break;
            default:
                throw new IllegalStateException("Unknown navigation item: " + item.getTitle());
        }

        startActivityWithAnimation(intent);
    }

    private void startActivityWithAnimation(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
    }

    protected Toolbar getToolbar() {
        return null;
    }

    protected ViewGroup getContentView() {
        return null == content ? container : content;
    }

    protected
    @IdRes
    int getSelfNavDrawerItem() {
        return NAVDRAWER_INVALID;
    }

    /**
     * required for debug view injection to work (else it has to reference context.getApplicationContext()
     *
     * @param name
     * @return
     */
    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return appGraph;
        }
        return super.getSystemService(name);
    }

    @Override
    protected void onDestroy() {
        appGraph = null;
        super.onDestroy();
    }

}
