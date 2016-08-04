package com.rockthevote.grommet.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.f2prateek.rx.preferences.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragment {

    @Inject
    @PartnerId
    Preference<String> partnerIdPref;

    @Inject
    @CanvasserName
    Preference<String> canvasserNamePref;

    @Inject
    @EventName
    Preference<String> eventNamePref;

    @Inject
    @EventZip
    Preference<String> eventZipPref;

    private CompositeSubscription subscriptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);

        getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName("grommet");

        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptions = new CompositeSubscription();

        subscriptions.add(partnerIdPref.asObservable().subscribe(id -> {
            findPreference(partnerIdPref.key()).setSummary(id);
        }));

        subscriptions.add(canvasserNamePref.asObservable().subscribe(name -> {
            findPreference(canvasserNamePref.key()).setSummary(name);
        }));

        subscriptions.add(eventNamePref.asObservable().subscribe(eventName -> {
            findPreference(eventNamePref.key()).setSummary(eventName);
        }));

        subscriptions.add(eventZipPref.asObservable().subscribe(zip -> {
            findPreference(eventZipPref.key()).setSummary(zip);
        }));
    }

    @Override
    public void onPause() {
        subscriptions.unsubscribe();
        super.onPause();
    }
}
