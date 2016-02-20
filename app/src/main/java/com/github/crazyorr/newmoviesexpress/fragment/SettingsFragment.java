package com.github.crazyorr.newmoviesexpress.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.widget.NumberPickerPreference;

/**
 * Created by wanglei02 on 2015/11/17.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        refreshPreference(getString(R.string.pref_key_notify_since));
        refreshPreference(getString(R.string.pref_key_notify_until));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        refreshPreference(key);
    }

    private void refreshPreference(String key) {
        Preference preference = findPreference(key);
        if (preference instanceof NumberPickerPreference) {
            NumberPickerPreference numberPickerPreference = (NumberPickerPreference) preference;
            int count = numberPickerPreference.getValue();
            String summary = null;
            if (key.equals(getString(R.string.pref_key_notify_since))) {
                summary = getResources().getQuantityString(R.plurals.days_before, count, count);
            } else if (key.equals(getString(R.string.pref_key_notify_until))) {
                summary = getResources().getQuantityString(R.plurals.days_after, count, count);
            }
            if (summary != null) {
                numberPickerPreference.setSummary(summary);
            }
        }
    }
}
