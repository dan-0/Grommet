package com.rockthevote.grommet.ui.settings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup contentView = getContentView();
        getLayoutInflater().inflate(R.layout.activity_settings, contentView);
        ButterKnife.bind(this, contentView);
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_settings;
    }
}
