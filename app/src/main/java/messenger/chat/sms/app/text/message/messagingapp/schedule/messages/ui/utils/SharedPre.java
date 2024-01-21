package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPre {

    public static final String IS_ALL_INITIAL_STEP_DONE = "is_all_initial_step_done";
    public static final String SELECTED_UNIT = "selected_unit";
    public static final String SAVE_NOTIFICATION_SOUND_PATH = "save_notification_sound_path";
    public static final String LANGUAGE = "language";
    public static final String IS_LANGUAGE_CHANGED = "language_changed";
    public static final String PREVIOUSLANGUAGE = "previous_language";
    public static final String REMINDER_MODE = "reminder_mode";
    public static final String IS_FURTHER_REMINDER_ON = "is_further_reminder_on";
    public static final String DAILY_INTAKE = "daily_intake";
    public static final String IS_NOTIFICATION_OFF = "is_notification_off";
    public static final String IS_8HR_ADS_REMOVE = "is_8hr_ads_remove";
    public static final String REMOVE_TIME = "remove_time";
    private static final String USER_PREFS = "MyPrefs";

    private static SharedPreferences getPrefEdit(Context context) {
        return context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE);
    }

    public static void save(Context context, String key, String value) {
        getPrefEdit(context).edit().putString(key, value).apply();
    }

    public static void save(Context context, String key, int value) {
        getPrefEdit(context).edit().putInt(key, value).apply();
    }

    public static void save(Context context, String key, boolean value) {
        getPrefEdit(context).edit().putBoolean(key, value).apply();
    }

    public static void save(Context context, String key, float value) {
        getPrefEdit(context).edit().putFloat(key, value).apply();
    }

    public static void save(Context context, String key, long value) {
        getPrefEdit(context).edit().putLong(key, value).apply();
    }

    public static String getString(Context context, String key) {
        return getPrefEdit(context).getString(key, "");
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getPrefEdit(context).getString(key, defaultValue);
    }

    public static int getInt(Context context, String key) {
        return getPrefEdit(context).getInt(key, 0);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return getPrefEdit(context).getInt(key, defaultValue);
    }

    public static long getLong(Context context, String key) {
        return getPrefEdit(context).getLong(key, 0);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getPrefEdit(context).getLong(key, defaultValue);
    }

    public static float getFloat(Context context, String key) {
        return getPrefEdit(context).getFloat(key, 0f);
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        return getPrefEdit(context).getFloat(key, defaultValue);
    }

    public static boolean getBoolean(Context context, String key) {
        return getPrefEdit(context).getBoolean(key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getPrefEdit(context).getBoolean(key, defaultValue);
    }
}

