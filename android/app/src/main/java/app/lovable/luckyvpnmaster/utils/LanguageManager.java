
package app.lovable.luckyvpnmaster.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LanguageManager {
    private static final String PREFERENCES_NAME = "app_preferences";
    private static final String LANGUAGE_KEY = "selected_language";
    
    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply();
        
        updateAppLanguage(context, languageCode);
    }
    
    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(LANGUAGE_KEY, "en");
    }
    
    public static void updateAppLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        
        context.createConfigurationContext(configuration);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
    
    public static void initializeLanguage(Context context) {
        String savedLanguage = getLanguage(context);
        updateAppLanguage(context, savedLanguage);
    }
}
