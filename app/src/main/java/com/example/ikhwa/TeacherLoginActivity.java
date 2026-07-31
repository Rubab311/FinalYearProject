package com.example.ikhwa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class TeacherLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, thrSignupBtn;
    private TextView errorText, forgetPasswordText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private ImageView passwordToggle;
    private DatabaseReference teacherRef;
    private boolean isPasswordVisible = false;
    private static final String TAG = "TeacherLoginActivity";

    private final String[] adminEmails = {"innovibes311@gmail.com", "hidayahclub100@gmail.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        try {
            mAuth = FirebaseAuth.getInstance();

            // Auto-login check
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                SharedPreferences prefs = getSharedPreferences(SplashActivity.PREF_NAME, MODE_PRIVATE);
                String savedRole = prefs.getString(SplashActivity.ROLE_KEY, "");

                if ("teacher".equals(savedRole) || "admin".equals(savedRole)) {
                    checkUserRole(currentUser.getUid(), currentUser.getEmail(), currentUser);
                }
            }

            // Views
            emailInput = findViewById(R.id.teacher_email);
            passwordInput = findViewById(R.id.teacher_password);
            loginButton = findViewById(R.id.teacher_btn_login);
            thrSignupBtn = findViewById(R.id.thr_sign_up);
            errorText = findViewById(R.id.error_message);
            progressBar = findViewById(R.id.progress_bar);
            forgetPasswordText = findViewById(R.id.forgot_password);
            passwordToggle = findViewById(R.id.password_toggle);

            teacherRef = FirebaseDatabase.getInstance().getReference("Teachers");

            if (forgetPasswordText != null) {
                forgetPasswordText.setOnClickListener(v -> {
                    startActivity(new Intent(this, ResetPasswordActivity.class));
                });
            }

            if (passwordToggle != null) {
                passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
            }

            loginButton.setOnClickListener(v -> validateTeacherLogin());

            if (thrSignupBtn != null) {
                thrSignupBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(this, TeacherRegistrationActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    private void togglePasswordVisibility() {
        Typeface currentTypeface = passwordInput.getTypeface();
        if (isPasswordVisible) {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_visible_off);
            isPasswordVisible = false;
        } else {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_password);
            isPasswordVisible = true;
        }
        passwordInput.setTypeface(currentTypeface);
        passwordInput.setSelection(passwordInput.length());
    }

    private void validateTeacherLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            // Admin bypass email verification
                            boolean isAdmin = false;
                            for (String adminEmail : adminEmails) {
                                if (adminEmail.equalsIgnoreCase(email)) {
                                    isAdmin = true;
                                    break;
                                }
                            }

                            if (!isAdmin && !user.isEmailVerified()) {
                                Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                return;
                            }

                            checkUserRole(user.getUid(), email, user);
                        }
                    } else {
                        Toast.makeText(this, "Login failed. Please check your email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String uid, String email, FirebaseUser user) {
        try {
            // Admin check
            for (String adminEmail : adminEmails) {
                if (adminEmail.equalsIgnoreCase(email)) {
                    handleAdminLogin();
                    return;
                }
            }

            // Teacher DB check
            Query teacherQuery = teacherRef.orderByChild("email").equalTo(email);
            teacherQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean approvedFound = false;
                        for (DataSnapshot teacherSnap : snapshot.getChildren()) {
                            com.example.ikhwa.TeacherModel teacher = teacherSnap.getValue(com.example.ikhwa.TeacherModel.class);
                            if (teacher != null && "approved".equalsIgnoreCase(teacher.getStatus())) {
                                approvedFound = true;
                                handleTeacherLogin();
                                break;
                            }
                        }
                        if (!approvedFound) {
                            showError("Your registration is not approved yet.");
                            mAuth.signOut();
                        }
                    } else {
                        showError("No teacher found with this email.");
                        mAuth.signOut();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showError("Database error. Try again.");
                }
            });

        } catch (Exception e) {
            showError("Error checking user role.");
            mAuth.signOut();
        }
    }

    private void handleAdminLogin() {
        SharedPreferences prefs = getSharedPreferences(SplashActivity.PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(SplashActivity.ROLE_KEY, "admin").apply();
        Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AdminHomeActivity.class));
        finish();
    }

    private void handleTeacherLogin() {
        SharedPreferences prefs = getSharedPreferences(SplashActivity.PREF_NAME, MODE_PRIVATE);
        prefs.edit().putString(SplashActivity.ROLE_KEY, "teacher").apply();
        Toast.makeText(this, "Teacher Login Successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, Teacher_home.class));
        finish();
    }

    private void showError(String message) {
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
