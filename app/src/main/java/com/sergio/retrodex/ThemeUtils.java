package com.sergio.retrodex;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public final class ThemeUtils {

    private ThemeUtils() {
    }

    public static void applySavedTheme(Context context) {
        boolean darkTheme = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean("dark_theme", true);

        AppCompatDelegate.setDefaultNightMode(
                darkTheme
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
