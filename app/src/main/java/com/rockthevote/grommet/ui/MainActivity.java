package com.rockthevote.grommet.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.ui.registration.EligibilityFragment;
import com.rockthevote.grommet.ui.registration.PersonalInfoFragment;
import com.rockthevote.grommet.ui.registration.RegisterFragment;
import com.rockthevote.grommet.ui.registration.RegistrationPagerAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.ObjectGraph;

public final class MainActivity extends Activity {

    @Inject ViewContainer viewContainer;

    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.tabLayout) TabLayout tabLayout;

    private ObjectGraph activityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();

        Injector.obtain(getApplication()).inject(this);
        ObjectGraph appGraph = Injector.obtain(getApplication());
        appGraph.inject(this);

        activityGraph = appGraph.plus(new MainActivityModule(this));

        ViewGroup container = viewContainer.forActivity(this);

        inflater.inflate(R.layout.activity_main, container);
        ButterKnife.bind(this, container);

        setupViewPager();
    }

    private void setupViewPager(){
        RegistrationPagerAdapter adapter = new RegistrationPagerAdapter(getFragmentManager());
        adapter.addFragment(new EligibilityFragment(), getString(R.string.fragment_title_eligibility));
        adapter.addFragment(new PersonalInfoFragment(), getString(R.string.fragment_title_personal_info));
        adapter.addFragment(new RegisterFragment(), getString(R.string.fragment_title_register));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return activityGraph;
        }
        return super.getSystemService(name);
    }

    @Override
    protected void onDestroy() {
        activityGraph = null;
        super.onDestroy();
    }
}
