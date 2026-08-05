package com.example.ikhwa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ikhwa.modules.TeacherDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StafftActivity extends AppCompatActivity {

    private LinearLayout teachersContainer;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stafft);

        backButton = findViewById(R.id.back_btn);
        teachersContainer = findViewById(R.id.teachers_container);

        backButton.setOnClickListener(v -> {
            finish();
        });

        loadStaff();
    }

    private void loadStaff() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Teachers");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                teachersContainer.removeAllViews();

                if (!snapshot.exists()) {
                    Toast.makeText(StafftActivity.this,
                            "No Staff Found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot snap : snapshot.getChildren()) {

                    TeacherDetails teacher = snap.getValue(TeacherDetails.class);

                    if (teacher == null) {
                        continue;
                    }

                    View itemView = LayoutInflater.from(StafftActivity.this)
                            .inflate(R.layout.teacher_list_item,
                                    teachersContainer,
                                    false);

                    TextView tvName = itemView.findViewById(R.id.tvName);
                    TextView tvFather = itemView.findViewById(R.id.tvFather);

                    tvName.setText(teacher.getName());

                    if (teacher.getFatherName() != null &&
                            !teacher.getFatherName().isEmpty()) {
                        tvFather.setText("Father's Name: " + teacher.getFatherName());
                    } else {
                        tvFather.setText("Father's Name: -");
                    }

                    teachersContainer.addView(itemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        StafftActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}