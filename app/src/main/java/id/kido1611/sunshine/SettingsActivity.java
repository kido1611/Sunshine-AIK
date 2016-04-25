package id.kido1611.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by Ahmad on 4/23/2016.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryTovalue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryTovalue(findPreference(getString(R.string.pref_units_key)));
    }

    private void bindPreferenceSummaryTovalue(Preference pref){
        pref.setOnPreferenceChangeListener(this);
        onPreferenceChange(pref, PreferenceManager.getDefaultSharedPreferences(pref.getContext())
            .getString(pref.getKey(),""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String stringValue = o.toString();
        if(preference instanceof ListPreference){
            ListPreference listPref = (ListPreference) preference;
            int prefIndex = listPref.findIndexOfValue(stringValue);
            if(prefIndex>=0){
                preference.setSummary(listPref.getEntries()[prefIndex]);
            }
        }else{
            preference.setSummary(stringValue);
        }
        return true;
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
