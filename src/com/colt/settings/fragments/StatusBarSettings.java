/*
 * Copyright (C) 2017 ColtOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.colt.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.UserHandle;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.internal.util.colt.ColtUtils;
import com.colt.settings.utils.TelephonyUtils

import com.colt.settings.preference.CustomSeekBarPreference;
import com.colt.settings.preference.SystemSettingSwitchPreference;
import com.colt.settings.preference.SystemSettingSeekBarPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class StatusBarSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_NETWORK_TRAFFIC = "network_traffic_location";
    private static final String KEY_NETWORK_TRAFFIC_ARROW = "network_traffic_arrow";
    private static final String KEY_NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide_threshold";
    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";

    private static final String KEY_SHOW_VOLTE = "volte_icon_style";

    private ListPreference mShowVolte;
    private ListPreference mNetworkTraffic;
    private SystemSettingSwitchPreference mNetworkTrafficArrow;
    private SystemSettingSeekBarPreference mNetworkTrafficAutohide;
    private SwitchPreference mUseOldMobileType;
    private boolean mConfigUseOldMobileType;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.statusbar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
	final ContentResolver resolver = getActivity().getContentResolver();

	mShowVolte = (ListPreference) findPreference(KEY_SHOW_VOLTE);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefSet.removePreference(mShowVolte);
        }

	mNetworkTraffic = (ListPreference) findPreference(KEY_NETWORK_TRAFFIC);
        int networkTraffic = Settings.System.getInt(resolver,
        Settings.System.NETWORK_TRAFFIC_LOCATION, 0);
        CharSequence[] NonNotchEntries = { getResources().getString(R.string.network_traffic_disabled),
                getResources().getString(R.string.network_traffic_statusbar),
                getResources().getString(R.string.network_traffic_qs_header) };
        CharSequence[] NotchEntries = { getResources().getString(R.string.network_traffic_disabled),
		getResources().getString(R.string.network_traffic_statusbar),
                getResources().getString(R.string.network_traffic_qs_header) };
        CharSequence[] NonNotchValues = {"0", "1" , "2"};
	CharSequence[] NotchValues = {"0", "1" , "2"};
        mNetworkTraffic.setEntries(ColtUtils.hasNotch(getActivity()) ? NotchEntries : NonNotchEntries);
        mNetworkTraffic.setEntryValues(ColtUtils.hasNotch(getActivity()) ? NotchValues : NonNotchValues);
        mNetworkTraffic.setValue(String.valueOf(networkTraffic));
        mNetworkTraffic.setSummary(mNetworkTraffic.getEntry());
        mNetworkTraffic.setOnPreferenceChangeListener(this);

        mNetworkTrafficArrow = (SystemSettingSwitchPreference) findPreference(KEY_NETWORK_TRAFFIC_ARROW);
	mNetworkTrafficAutohide = (SystemSettingSeekBarPreference) findPreference(KEY_NETWORK_TRAFFIC_AUTOHIDE);
        updateNetworkTrafficPrefs(networkTraffic);

	mConfigUseOldMobileType = getResources().getBoolean(com.android.internal.R.bool.config_useOldMobileIcons);
        int useOldMobileIcons = (!mConfigUseOldMobileType ? 1 : 0);
        mUseOldMobileType = (SwitchPreference) findPreference(KEY_USE_OLD_MOBILETYPE);
        mUseOldMobileType.setChecked((Settings.System.getInt(resolver,
                Settings.System.USE_OLD_MOBILETYPE, useOldMobileIcons) == 1));
        mUseOldMobileType.setOnPreferenceChangeListener(this);
   }

 @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mNetworkTraffic) {
            int networkTraffic = Integer.valueOf((String) objValue);
            int index = mNetworkTraffic.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_LOCATION, networkTraffic);
            mNetworkTraffic.setSummary(mNetworkTraffic.getEntries()[index]);
            updateNetworkTrafficPrefs(networkTraffic);
            return true;
	} else if (preference == mUseOldMobileType) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.USE_OLD_MOBILETYPE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateNetworkTrafficPrefs(int networkTraffic) {
        if (mNetworkTraffic != null) {
            if (networkTraffic == 0) {
                mNetworkTrafficArrow.setEnabled(false);
                mNetworkTrafficAutohide.setEnabled(false);
            } else {
                mNetworkTrafficArrow.setEnabled(true);
                mNetworkTrafficAutohide.setEnabled(true);
            }
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();

        Settings.System.putIntForUser(resolver,
		Settings.System.VOLTE_ICON_STYLE, 0, UserHandle.USER_CURRENT);
    }

@Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
 }
