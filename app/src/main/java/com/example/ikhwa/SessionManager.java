package com.example.ikhwa;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "FamilySession";
    private static final String KEY_MEMBER_ID = "currentMemberId";

    private static String currentMemberId = "";

    // Save selected member
    public static void setCurrentMemberId(Context context, String memberId) {

        currentMemberId = memberId;

        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_MEMBER_ID, memberId);
        editor.apply();
    }

    // Get selected member
    public static String getCurrentMemberId(Context context) {

        if (currentMemberId == null || currentMemberId.isEmpty()) {

            SharedPreferences preferences =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            currentMemberId =
                    preferences.getString(KEY_MEMBER_ID, "");
        }

        return currentMemberId;
    }

    // Clear selected member
    public static void clear(Context context) {

        currentMemberId = "";

        SharedPreferences preferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_MEMBER_ID);
        editor.apply();
    }
}