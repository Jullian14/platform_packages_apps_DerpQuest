/*
 * Copyright (C) 2017-2019 The PixelDust Project
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
package com.derpquest.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.derpquest.settings.preferences.SecureSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingListPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;
import com.msm.xtended.preferences.CustomSeekBarPreference;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private static final String LOCKSCREEN_ALBUM_ART_FILTER = "lockscreen_album_art_filter";
    private static final String LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";

    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";
    private static final String CLOCK_FONT_SIZE  = "lockclock_font_size";
    private static final String DATE_FONT_SIZE  = "lockdate_font_size";


    private ListPreference mLockClockFonts;
    private ListPreference mLockDateFonts;
    private CustomSeekBarPreference mClockFontSize;
    private CustomSeekBarPreference mDateFontSize;

    private SecureSettingMasterSwitchPreference mVisualizerEnabled;
    private SystemSettingListPreference mArtFilter;
    private SystemSettingSeekBarPreference mBlurSeekbar;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_lockscreen);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (!mFingerprintManager.isHardwareDetected()){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

        mArtFilter = (SystemSettingListPreference) findPreference(LOCKSCREEN_ALBUM_ART_FILTER);
        mArtFilter.setOnPreferenceChangeListener(this);
        int artFilter = Settings.System.getInt(resolver,
                LOCKSCREEN_ALBUM_ART_FILTER, 0);
        mBlurSeekbar = (SystemSettingSeekBarPreference) findPreference(LOCKSCREEN_MEDIA_BLUR);
        mBlurSeekbar.setEnabled(artFilter > 2);

        // Lockscren Clock Fonts
        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONTS, 34)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        // Lockscren Date Fonts
        mLockDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mLockDateFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_DATE_FONTS, 32)));
        mLockDateFonts.setSummary(mLockDateFonts.getEntry());
        mLockDateFonts.setOnPreferenceChangeListener(this);

        // Lock Clock Size
        mClockFontSize = (CustomSeekBarPreference) findPreference(CLOCK_FONT_SIZE);
        mClockFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKCLOCK_FONT_SIZE, 78));
        mClockFontSize.setOnPreferenceChangeListener(this);

        // Lock Date Size
        mDateFontSize = (CustomSeekBarPreference) findPreference(DATE_FONT_SIZE);
        mDateFontSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKDATE_FONT_SIZE, 18));
        mDateFontSize.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mArtFilter) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALBUM_ART_FILTER, value);
            mBlurSeekbar.setEnabled(value > 2);
            return true;
        }else if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mLockDateFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) newValue));
            mLockDateFonts.setValue(String.valueOf(newValue));
            mLockDateFonts.setSummary(mLockDateFonts.getEntry());
            return true;
        } else if (preference == mClockFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKCLOCK_FONT_SIZE, top*1);
            return true;
        } else if (preference == mDateFontSize) {
            int top = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKDATE_FONT_SIZE, top*1);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }
}
