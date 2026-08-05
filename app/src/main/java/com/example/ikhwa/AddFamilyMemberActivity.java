package com.example.ikhwa;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddFamilyMemberActivity extends AppCompatActivity {

    private EditText etMemberName, etMemberAge;
    private Spinner spGender, spRelationship;
    private Button btnAdd;

    private DatabaseReference familyRef;
    private DatabaseReference studentRef;
    private DatabaseReference usersRef;
    private DatabaseReference enrollmentsRef;

    private String ownerUid;
    private EditText etFatherName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_family_member);

        etMemberName = findViewById(R.id.etMemberName);
        etMemberAge = findViewById(R.id.etMemberAge);
        spGender = findViewById(R.id.spGender);
        spRelationship = findViewById(R.id.spRelationship);
        btnAdd = findViewById(R.id.btnAdd);
        etFatherName = findViewById(R.id.etFatherName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);

        ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        familyRef = FirebaseDatabase.getInstance()
                .getReference("FamilyAccounts")
                .child(ownerUid)
                .child("members");

        studentRef = FirebaseDatabase.getInstance()
                .getReference("Student");

        usersRef = FirebaseDatabase.getInstance()
                .getReference("Users");

        enrollmentsRef = FirebaseDatabase.getInstance()
                .getReference("Enrollments");

        // Gender Spinner
        String[] genders = {
                "Male",
                "Female"
        };

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                genders
        );

        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spGender.setAdapter(genderAdapter);
        // Relationship Spinner
        String[] relationships = {
                "Son",
                "Daughter",
                "Brother",
                "Sister",
                "Mother",
                "Father",
                "Wife",
                "Husband",
                "Other"
        };

        ArrayAdapter<String> relationAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                relationships
        );

        relationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spRelationship.setAdapter(relationAdapter);
        btnAdd.setOnClickListener(v -> addFamilyMember());
    }
    private void addFamilyMember() {

        String name = etMemberName.getText().toString().trim();
        String age = etMemberAge.getText().toString().trim();
        String gender = spGender.getSelectedItem().toString();
        String relationship = spRelationship.getSelectedItem().toString();
        String fatherName = etFatherName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etMemberName.setError("Enter member name");
            etMemberName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(age)) {
            etMemberAge.setError("Enter age");
            etMemberAge.requestFocus();
            return;
        }

        // Generate unique Member ID
        String memberId = familyRef.push().getKey();

        if (memberId == null) {
            Toast.makeText(this, "Unable to create member.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ==========================
        // Family Member Data
        // ==========================

        HashMap<String, Object> memberMap = new HashMap<>();

        memberMap.put("memberId", memberId);
        memberMap.put("name", name);
        memberMap.put("age", age);
        memberMap.put("gender", gender);
        memberMap.put("relationship", relationship);
        memberMap.put("ownerUid", ownerUid);

        // ==========================
        // Student Profile Data
        // ==========================

        HashMap<String, Object> studentMap = new HashMap<>();

        studentMap.put("student_id", memberId);
        studentMap.put("student_name", name);
        studentMap.put("father_name", fatherName);
        studentMap.put("phone", phone);
        studentMap.put("address", address);
        studentMap.put("email", email);
        studentMap.put("age", age);
        studentMap.put("gender", gender);
        studentMap.put("relationship", relationship);
        studentMap.put("ownerUid", ownerUid);

        // ==========================
        // Users Data
        // ==========================

        HashMap<String, Object> userMap = new HashMap<>();

        userMap.put("attemptedLectures", "");
        userMap.put("certificates", "");
        userMap.put("ownerUid", ownerUid);

        // Empty Enrollment Node
        HashMap<String, Object> enrollmentMap = new HashMap<>();
        // ==========================
        // Save Family Member
        // ==========================

        familyRef.child(memberId).setValue(memberMap)

                .addOnSuccessListener(unused -> {

                    // Save Student Profile
                    studentRef.child(memberId).setValue(studentMap)

                            .addOnSuccessListener(unused1 -> {

                                // Save User Data
                                usersRef.child(memberId).setValue(userMap)

                                        .addOnSuccessListener(unused2 -> {

                                            // Create Empty Enrollment Node
                                            enrollmentsRef.child(memberId)
                                                    .setValue(enrollmentMap)

                                                    .addOnSuccessListener(unused3 -> {

                                                        Toast.makeText(
                                                                AddFamilyMemberActivity.this,
                                                                "Family Member Added Successfully",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        finish();

                                                    })

                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(
                                                                    AddFamilyMemberActivity.this,
                                                                    e.getMessage(),
                                                                    Toast.LENGTH_SHORT
                                                            ).show()
                                                    );

                                        })

                                        .addOnFailureListener(e ->
                                                Toast.makeText(
                                                        AddFamilyMemberActivity.this,
                                                        e.getMessage(),
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        );

                            })

                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            AddFamilyMemberActivity.this,
                                            e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );

                })

                .addOnFailureListener(e ->
                        Toast.makeText(
                                AddFamilyMemberActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}