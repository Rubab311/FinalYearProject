package com.example.ikhwa;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ikhwa.modules.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StdEditProfileActivity extends AppCompatActivity {

    private EditText etStudentNameEdit;
    private EditText etFatherNameEdit;
    private EditText etNumberEdit;
    private EditText etAgeEdit;
    private EditText etAddressEdit;
    private EditText etEmailEdit;

    private Button btnSave;

    private FirebaseAuth mAuth;
    private DatabaseReference studentRef;
    private DatabaseReference familyRef;

    private String uid;
    private String ownerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_std_edit_profile);

        etStudentNameEdit = findViewById(R.id.etStuNameEdit);
        etFatherNameEdit = findViewById(R.id.etFatherNameEdit);
        etNumberEdit = findViewById(R.id.etNumberEdit);
        etAgeEdit = findViewById(R.id.etAgeEdit);
        etAddressEdit = findViewById(R.id.etAddressEdit);
        etEmailEdit = findViewById(R.id.etEmailEdit);

        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ownerUid = mAuth.getCurrentUser().getUid();

        uid = SessionManager.getCurrentMemberId(this);

        if (uid == null || uid.isEmpty()) {
            uid = ownerUid;
        }

        studentRef = FirebaseDatabase.getInstance()
                .getReference("Student");

        familyRef = FirebaseDatabase.getInstance()
                .getReference("FamilyAccounts")
                .child(ownerUid)
                .child("members");

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());

    }
    private void loadProfile() {

        studentRef.child(uid)
                .get()
                .addOnSuccessListener(dataSnapshot -> {

                    if (!dataSnapshot.exists()) {

                        Toast.makeText(
                                StdEditProfileActivity.this,
                                "No profile found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Student student = dataSnapshot.getValue(Student.class);

                    if (student == null) {
                        return;
                    }

                    etStudentNameEdit.setText(student.getStudent_name());
                    etFatherNameEdit.setText(student.getFather_name());
                    etNumberEdit.setText(student.getPhone());
                    etAgeEdit.setText(student.getAge());
                    etAddressEdit.setText(student.getAddress());
                    etEmailEdit.setText(student.getEmail());

                })
                .addOnFailureListener(e ->

                        Toast.makeText(
                                StdEditProfileActivity.this,
                                "Failed to load profile",
                                Toast.LENGTH_SHORT
                        ).show()

                );

    }

    private void saveProfile() {

        String studentName =
                etStudentNameEdit.getText().toString().trim();

        String fatherName =
                etFatherNameEdit.getText().toString().trim();

        String phone =
                etNumberEdit.getText().toString().trim();

        String age =
                etAgeEdit.getText().toString().trim();

        String address =
                etAddressEdit.getText().toString().trim();

        String email =
                etEmailEdit.getText().toString().trim();

        if (TextUtils.isEmpty(studentName)
                || TextUtils.isEmpty(fatherName)
                || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(age)
                || TextUtils.isEmpty(address)
                || TextUtils.isEmpty(email)) {

            Toast.makeText(
                    StdEditProfileActivity.this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSave.setEnabled(false);
        studentRef.child(uid).child("student_name").setValue(studentName);
        studentRef.child(uid).child("father_name").setValue(fatherName);
        studentRef.child(uid).child("phone").setValue(phone);
        studentRef.child(uid).child("age").setValue(age);
        studentRef.child(uid).child("address").setValue(address);
        studentRef.child(uid).child("email").setValue(email);

        // ==========================
        // Update Family Member List
        // ==========================

        if (!uid.equals(ownerUid)) {

            familyRef.child(uid).child("name").setValue(studentName);
            familyRef.child(uid).child("age").setValue(age);

        }

        btnSave.setEnabled(true);

        Toast.makeText(
                StdEditProfileActivity.this,
                "Profile updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();

        uid = SessionManager.getCurrentMemberId(this);

        if (uid == null || uid.isEmpty()) {
            uid = ownerUid;
        }

        loadProfile();
    }
}