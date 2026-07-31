package com.example.ikhwa;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FamilyAccountActivity extends AppCompatActivity
        implements FamilyMemberAdapter.OnMemberActionListener {

    private TextView tvFamilyName;
    private Button btnAddMember;
    private RecyclerView recyclerMembers;

    private ArrayList<MemberModel> memberList;
    private FamilyMemberAdapter adapter;

    private DatabaseReference familyRef;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_account);

        tvFamilyName = findViewById(R.id.tvFamilyName);
        btnAddMember = findViewById(R.id.btnAddMember);
        recyclerMembers = findViewById(R.id.recyclerMembers);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        familyRef = FirebaseDatabase.getInstance()
                .getReference("FamilyAccounts")
                .child(uid);

        memberList = new ArrayList<>();

        adapter = new FamilyMemberAdapter(memberList, this);

        recyclerMembers.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerMembers.setAdapter(adapter);

        loadFamily();

        btnAddMember.setOnClickListener(v -> {

            startActivity(new Intent(
                    FamilyAccountActivity.this,
                    AddFamilyMemberActivity.class));

        });

    }

    //==========================
    // Switch Button Click
    //==========================

    @Override
    public void onSwitch(MemberModel member) {

        SessionManager.setCurrentMemberId(
                FamilyAccountActivity.this,
                member.getId());

        familyRef.child("selectedMember")
                .setValue(member.getId())
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Switched to " + member.getName(),
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            FamilyAccountActivity.this,
                            StudentHome2.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    finish();

                });

    }

    //==========================
    // Delete Button Click
    //==========================

    @Override
    public void onDelete(MemberModel member) {

        if (memberList.size() <= 1) {

            Toast.makeText(
                    this,
                    "At least one member is required.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Member")
                .setMessage("Remove " + member.getName() + " from family?")
                .setPositiveButton("Delete",
                        (dialog, which) -> deleteMember(member))
                .setNegativeButton("Cancel", null)
                .show();

    }
    //==========================
// Load Family Members
//==========================

    private void loadFamily() {

        familyRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                memberList.clear();

                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                String familyName = snapshot.child("familyName")
                        .getValue(String.class);

                if (familyName != null) {
                    tvFamilyName.setText(familyName);
                }

                DataSnapshot membersSnapshot = snapshot.child("members");

                for (DataSnapshot ds : membersSnapshot.getChildren()) {

                    String id = ds.getKey();

                    String name = ds.child("name")
                            .getValue(String.class);

                    String relation = ds.child("relationship")
                            .getValue(String.class);

                    if (name == null) name = "Unknown";
                    if (relation == null) relation = "";

                    memberList.add(new MemberModel(
                            id,
                            name,
                            relation
                    ));
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        FamilyAccountActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

            }
        });

    }


//==========================
// Delete Member
//==========================

    private void deleteMember(MemberModel member) {

        familyRef.child("members")
                .child(member.getId())
                .removeValue()
                .addOnSuccessListener(unused -> {

                    // Agar deleted member selected tha to selectedMember bhi remove kar do
                    familyRef.child("selectedMember")
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                String selected =
                                        snapshot.getValue(String.class);

                                if (selected != null &&
                                        selected.equals(member.getId())) {

                                    familyRef.child("selectedMember")
                                            .removeValue();

                                    SessionManager.setCurrentMemberId(
                                            FamilyAccountActivity.this,
                                            ""
                                    );

                                }

                            });

                    Toast.makeText(
                            FamilyAccountActivity.this,
                            "Member deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e ->

                        Toast.makeText(
                                FamilyAccountActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());

    }

}