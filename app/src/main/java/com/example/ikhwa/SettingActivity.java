package com.example.ikhwa;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {

    private SwitchCompat notificationsSwitch;

    private View layoutPrivacyPolicy,
            layoutTerms,
            layoutShare,
            layoutRate,
            layoutLogout;

    private FloatingActionButton fabLogout;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        authProfile = FirebaseAuth.getInstance();

        // Initialize UI elements
        notificationsSwitch = findViewById(R.id.switch_notifications);
        layoutPrivacyPolicy = findViewById(R.id.layout_privacy_policy);
        layoutTerms = findViewById(R.id.layout_terms);
        layoutShare = findViewById(R.id.layout_share);
        layoutRate = findViewById(R.id.layout_rate);
        layoutLogout = findViewById(R.id.layout_logout);

        setupListeners();
        loadNotificationPreference();
    }

    private void setupListeners() {

        notificationsSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView,
                            boolean isChecked) {

                        saveNotificationPreference(isChecked);

                        showToast(
                                isChecked
                                        ? "Notifications enabled"
                                        : "Notifications disabled"
                        );
                    }
                }
        );

        // Privacy Policy
        layoutPrivacyPolicy.setOnClickListener(
                v -> navigateToPage("privacy_policy")
        );

        // Terms & Conditions
        layoutTerms.setOnClickListener(
                v -> navigateToPage("terms_conditions")
        );

        // Share App
        layoutShare.setOnClickListener(
                v -> shareApp()
        );

        // Rate App
        layoutRate.setOnClickListener(
                v -> rateApp()
        );

        // Logout
        layoutLogout.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        SharedPreferences prefs =
                                getSharedPreferences(
                                        SplashActivity.PREF_NAME,
                                        MODE_PRIVATE
                                );

                        prefs.edit()
                                .remove(SplashActivity.ROLE_KEY)
                                .apply();

                        FirebaseAuth.getInstance().signOut();

                        Intent intent =
                                new Intent(
                                        SettingActivity.this,
                                        SelectionActivity.class
                                );

                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                        );

                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void saveNotificationPreference(boolean enabled) {

        getSharedPreferences(
                "app_prefs",
                MODE_PRIVATE
        )
                .edit()
                .putBoolean(
                        "notifications_enabled",
                        enabled
                )
                .apply();
    }

    private void loadNotificationPreference() {

        boolean enabled =
                getSharedPreferences(
                        "app_prefs",
                        MODE_PRIVATE
                )
                        .getBoolean(
                                "notifications_enabled",
                                true
                        );

        notificationsSwitch.setChecked(enabled);
    }

    private void navigateToPage(String page) {

        switch (page) {

            case "privacy_policy":
                showToast("Opening Privacy Policy");
                startActivity(
                        new Intent(
                                this,
                                PrivacyPolicyActivity4.class
                        )
                );
                break;

            case "terms_conditions":
                showToast("Opening Terms and Conditions");
                startActivity(
                        new Intent(
                                this,
                                TermsActivity.class
                        )
                );
                break;
        }
    }

    private void shareApp() {

        Intent shareIntent =
                new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "Check out this amazing app!"
        );

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey! I'm using this awesome app. Check it out: " +
                        "https://play.google.com/store/apps/details?id="
                        + getPackageName()
        );

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share via"
                )
        );
    }

    private void rateApp() {

        showToast(
                "Opening app in Play Store for rating"
        );

        try {

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "market://details?id="
                                            + getPackageName()
                            )
                    )
            );

        } catch (ActivityNotFoundException e) {

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://play.google.com/store/apps/details?id="
                                            + getPackageName()
                            )
                    )
            );
        }
    }

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(
                        "Yes",
                        (dialog, which) -> {

                            getSharedPreferences(
                                    "app_prefs",
                                    MODE_PRIVATE
                            )
                                    .edit()
                                    .clear()
                                    .apply();

                            getSharedPreferences(
                                    SplashActivity.PREF_NAME,
                                    MODE_PRIVATE
                            )
                                    .edit()
                                    .clear()
                                    .apply();

                            authProfile.signOut();

                            showToast(
                                    "Logged out successfully."
                            );

                            Intent intent =
                                    new Intent(
                                            this,
                                            SelectionActivity.class
                                    );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                            Intent.FLAG_ACTIVITY_NEW_TASK
                            );

                            startActivity(intent);
                            finish();
                        }
                )
                .setNegativeButton(
                        "No",
                        (dialog, which) -> dialog.dismiss()
                )
                .create()
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}