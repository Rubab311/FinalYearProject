package com.example.ikhwa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ikhwa.modules.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class stdprofile extends AppCompatActivity {

    private EditText etName;
    private EditText etFatherName;
    private EditText etNumber;
    private EditText etAge;
    private EditText etAddress;
    private EditText etEmail;

    private TextView tvStudentName;

    private Button btnEditprofile;
    private Button btnFamilyAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stdprofile);

        // Views
        etName = findViewById(R.id.etName);
        etFatherName = findViewById(R.id.etFatherName);
        etNumber = findViewById(R.id.etNumber);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);
        etEmail = findViewById(R.id.etEmail);

        tvStudentName = findViewById(R.id.tvStudentName);

        btnEditprofile = findViewById(R.id.btnEditprofile);
        btnFamilyAccount = findViewById(R.id.btnFamilyAccount);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Current selected member
        uid = SessionManager.getCurrentMemberId(this);

        if (uid == null || uid.isEmpty()) {
            uid = mAuth.getCurrentUser().getUid();
        }

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Student");

        loadProfile();

        disableEditing();
        // ==========================
        // Edit Profile
        // ==========================

        btnEditprofile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    stdprofile.this,
                    StdEditProfileActivity.class
            );

            startActivity(intent);

        });

        // ==========================
        // Family Account
        // ==========================

        btnFamilyAccount.setOnClickListener(v -> {

            DatabaseReference familyRef = FirebaseDatabase.getInstance()
                    .getReference("FamilyAccounts")
                    .child(mAuth.getCurrentUser().getUid());

            familyRef.get().addOnSuccessListener(snapshot -> {

                Intent intent;

                if (snapshot.exists()) {

                    intent = new Intent(
                            stdprofile.this,
                            FamilyAccountActivity.class
                    );

                } else {

                    intent = new Intent(
                            stdprofile.this,
                            CreateFamilyActivity.class
                    );

                }

                startActivity(intent);

            }).addOnFailureListener(e ->

                    Toast.makeText(
                            stdprofile.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show()

            );

        });

    }

    // ==========================
    // Load Profile
    // ==========================

    private void loadProfile() {

        databaseReference.child(uid)
                .get()
                .addOnSuccessListener(dataSnapshot -> {

                    if (!dataSnapshot.exists()) {

                        Toast.makeText(
                                stdprofile.this,
                                "Profile not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Student student = dataSnapshot.getValue(Student.class);

                    if (student == null) {
                        return;
                    }

                    tvStudentName.setText(
                            student.getStudent_name() == null ? "" : student.getStudent_name()
                    );

                    etName.setText(
                            student.getStudent_name() == null ? "" : student.getStudent_name()
                    );

                    etFatherName.setText(
                            student.getFather_name() == null ? "" : student.getFather_name()
                    );
                    etNumber.setText(
                            student.getPhone() == null
                                    ? ""
                                    : student.getPhone()
                    );

                    if (student.getAge() != null) {
                        etAge.setText(String.valueOf(student.getAge()));
                    } else {
                        etAge.setText("");
                    }

                    etAddress.setText(
                            student.getAddress() == null
                                    ? ""
                                    : student.getAddress()
                    );

                    etEmail.setText(
                            student.getEmail() == null
                                    ? ""
                                    : student.getEmail()
                    );

                })

                .addOnFailureListener(e ->

                        Toast.makeText(
                                stdprofile.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()

                );

    }

    // ==========================
    // Disable Editing
    // ==========================

    private void disableEditing() {

        etName.setEnabled(false);
        etFatherName.setEnabled(false);
        etNumber.setEnabled(false);
        etAge.setEnabled(false);
        etAddress.setEnabled(false);
        etEmail.setEnabled(false);

    }
    @Override
    protected void onResume() {
        super.onResume();

        // Current selected member
        uid = SessionManager.getCurrentMemberId(this);

        if (uid == null || uid.isEmpty()) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        loadProfile();
    }
}