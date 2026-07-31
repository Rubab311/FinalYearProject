package com.example.ikhwa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentLoginActivity extends AppCompatActivity {

    EditText stEmail, stPassword;
    Button stLoginBtn, stSignupBtn;
    ProgressBar progressBar;
    TextView errorMessage, resetPassword;
    ImageView passwordToggle;
    FirebaseAuth mAuth;
    boolean isPasswordVisible = false;
    private static final String TAG = "StudentLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        stEmail = findViewById(R.id.st_email);
        stPassword = findViewById(R.id.st_password);
        stLoginBtn = findViewById(R.id.st_btn_login);
        stSignupBtn = findViewById(R.id.st_sign_up);
        progressBar = findViewById(R.id.progress_bar);
        errorMessage = findViewById(R.id.error_message);
        passwordToggle = findViewById(R.id.password_toggle);
        resetPassword = findViewById(R.id.forgot_password);

        mAuth = FirebaseAuth.getInstance();

        // Auto-login check
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            SharedPreferences prefs = getSharedPreferences(SplashActivity.PREF_NAME, MODE_PRIVATE);
            String savedRole = prefs.getString(SplashActivity.ROLE_KEY, "");

            if ("student".equals(savedRole)) {
                Log.d(TAG, "Current user is a student, navigating to StudentHome2");
                startActivity(new Intent(StudentLoginActivity.this, StudentHome2.class));
                finish();
            }
        }

        // Password toggle click event
        passwordToggle.setOnClickListener(v -> {
            Typeface currentTypeface = stPassword.getTypeface();

            if (isPasswordVisible) {
                stPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_visible_off);
                isPasswordVisible = false;
            } else {
                stPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_password);
                isPasswordVisible = true;
            }

            stPassword.setTypeface(currentTypeface);
            stPassword.setSelection(stPassword.length());
        });

        stLoginBtn.setOnClickListener(v -> loginStudent());

        stSignupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(StudentLoginActivity.this, StudentRegistrationActivity.class);
            startActivity(intent);
            finish();
        });

        resetPassword.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(StudentLoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting ResetPasswordActivity", e);
            }
        });
    }

    private void loginStudent() {
        String email = stEmail.getText().toString().trim();
        String password = stPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("All fields are required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            if (user.isEmailVerified()) {
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                SessionManager.setCurrentMemberId(StudentLoginActivity.this, uid);
                                // Email verified → allow login
                                Toast.makeText(StudentLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                SharedPreferences prefs = getSharedPreferences(SplashActivity.PREF_NAME, MODE_PRIVATE);
                                prefs.edit().clear().apply();
                                prefs.edit().putString(SplashActivity.ROLE_KEY, "student").apply();

                                Log.d(TAG, "Saved role 'student' in SharedPreferences");

                                Intent intent = new Intent(StudentLoginActivity.this, StudentHome2.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Email not verified
                                showError("Please verify your email before login.");
                                user.sendEmailVerification()
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(StudentLoginActivity.this, "Verification email sent again. Please check inbox.", Toast.LENGTH_LONG).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(StudentLoginActivity.this, "Failed to send verification email: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                mAuth.signOut(); // logout until verified
                            }
                        }

                    } else {
                        showError("Invalid email or password");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Error: " + e.getMessage());
                });
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
    }
}
