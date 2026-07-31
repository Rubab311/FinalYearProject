package com.example.ikhwa;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateFamilyActivity extends AppCompatActivity {

    private EditText etFamilyName;
    private EditText etOwnerName;

    private Button btnCreateFamily;

    private DatabaseReference familyRef;
    private DatabaseReference studentRef;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_family);

        etFamilyName = findViewById(R.id.etFamilyName);
        etOwnerName = findViewById(R.id.etOwnerName);
        btnCreateFamily = findViewById(R.id.btnCreateFamily);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        familyRef = FirebaseDatabase.getInstance()
                .getReference("FamilyAccounts");

        studentRef = FirebaseDatabase.getInstance()
                .getReference("Student");

        btnCreateFamily.setOnClickListener(v -> {

            createFamily();

        });

    }

    private void createFamily() {

        String familyName = etFamilyName.getText().toString().trim();
        String ownerName = etOwnerName.getText().toString().trim();

        if (TextUtils.isEmpty(familyName)) {
            etFamilyName.setError("Enter Family Name");
            return;
        }

        if (TextUtils.isEmpty(ownerName)) {
            etOwnerName.setError("Enter Mother / Guardian Name");
            return;
        }

        HashMap<String, Object> familyMap = new HashMap<>();

        familyMap.put("familyName", familyName);
        familyMap.put("ownerName", ownerName);
        familyMap.put("ownerUid", uid);
        familyMap.put("selectedMember", uid);
        familyRef.child(uid)
                .setValue(familyMap)
                .addOnSuccessListener(unused -> {

                    studentRef.child(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                String studentName = snapshot.child("student_name")
                                        .getValue(String.class);

                                if (studentName == null || studentName.isEmpty()) {
                                    studentName = ownerName;
                                }

                                HashMap<String, Object> memberMap = new HashMap<>();

                                memberMap.put("uid", uid);
                                memberMap.put("name", studentName);
                                memberMap.put("relationship", "Self");
                                memberMap.put("age", snapshot.child("age").getValue());
                                memberMap.put("gender", snapshot.child("gender").getValue());

                                familyRef.child(uid)
                                        .child("members")
                                        .child(uid)
                                        .setValue(memberMap)
                                        .addOnSuccessListener(aVoid -> {

                                            Toast.makeText(
                                                    CreateFamilyActivity.this,
                                                    "Family Account Created Successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            Intent intent = new Intent(
                                                    CreateFamilyActivity.this,
                                                    FamilyAccountActivity.class
                                            );

                                            startActivity(intent);
                                            finish();

                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(
                                                        CreateFamilyActivity.this,
                                                        e.getMessage(),
                                                        Toast.LENGTH_SHORT
                                                ).show());

                            });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                CreateFamilyActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());

    }
}