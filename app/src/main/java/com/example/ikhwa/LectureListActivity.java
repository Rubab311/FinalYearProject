package com.example.ikhwa;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LectureListActivity extends AppCompatActivity {

    private LinearLayout lectureListContainer;

    private String courseId;
    private String userId;
    private final ActivityResultLauncher<Intent> lectureLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK) {

                            loadLectures();

                        }

                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_list);

        ImageView backBtn = findViewById(R.id.backBtn);
        lectureListContainer = findViewById(R.id.lectureListContainer);

        backBtn.setOnClickListener(v -> finish());

        courseId = getIntent().getStringExtra("courseId");

        // Current Selected Member
        userId = SessionManager.getCurrentMemberId(this);

        if (userId == null || userId.isEmpty()) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (userId == null || userId.isEmpty()) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (courseId != null) {
            loadLectures();
        }
    }

    private void loadLectures() {

        lectureListContainer.removeAllViews();

        DatabaseReference lectureRef = FirebaseDatabase.getInstance()
                .getReference("Courses")
                .child(courseId)
                .child("lectures");

        DatabaseReference attemptRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("attemptedLectures")
                .child(courseId);
        attemptRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot attemptSnapshot) {

                lectureRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int count = 1;

                        for (DataSnapshot lectureSnap : snapshot.getChildren()) {

                            String lectureId = lectureSnap.getKey();
                            String title = lectureSnap.child("title").getValue(String.class);

                            boolean isAttempted = attemptSnapshot.hasChild(lectureId);

                            View lectureView = getLayoutInflater().inflate(
                                    R.layout.lecture_card,
                                    lectureListContainer,
                                    false
                            );

                            CardView card = lectureView.findViewById(R.id.cardLecture);

                            TextView txtTitle = lectureView.findViewById(R.id.txtLectureTitle);
                            TextView txtAttempt = lectureView.findViewById(R.id.txtAttempted);

                            txtTitle.setText("Lecture " + count + "\n" + title);

                            if (isAttempted) {
                                txtAttempt.setVisibility(View.VISIBLE);
                            } else {
                                txtAttempt.setVisibility(View.GONE);
                            }

                            ImageView imgBook = lectureView.findViewById(R.id.imgBook);
                            LinearLayout iconBg = lectureView.findViewById(R.id.iconBg);

                            int[] bgColors = {
                                    Color.parseColor("#E3F2FD"),
                                    Color.parseColor("#E8F5E9"),
                                    Color.parseColor("#FFF3E0"),
                                    Color.parseColor("#F3E5F5"),
                                    Color.parseColor("#FCE4EC"),
                                    Color.parseColor("#FFFDE7"),
                                    Color.parseColor("#E0F7FA")
                            };

                            int[] iconColors = {
                                    Color.parseColor("#1976D2"),
                                    Color.parseColor("#2E7D32"),
                                    Color.parseColor("#EF6C00"),
                                    Color.parseColor("#8E24AA"),
                                    Color.parseColor("#D81B60"),
                                    Color.parseColor("#F9A825"),
                                    Color.parseColor("#00838F")
                            };

                            GradientDrawable drawable =
                                    (GradientDrawable) iconBg.getBackground().mutate();

                            drawable.setColor(bgColors[(count - 1) % bgColors.length]);

                            imgBook.setColorFilter(iconColors[(count - 1) % iconColors.length]);

                            card.setOnClickListener(v -> {

                                Intent intent = new Intent(
                                        LectureListActivity.this,
                                        LectureDetailActivity.class);

                                intent.putExtra("courseId", courseId);
                                intent.putExtra("lectureId", lectureId);

                                lectureLauncher.launch(intent);
                            });

                            lectureListContainer.addView(lectureView);

                            count++;
                        }

                        if (snapshot.getChildrenCount() == 0) {

                            TextView empty = new TextView(LectureListActivity.this);

                            empty.setText("No Lectures Available");
                            empty.setTextSize(18);
                            empty.setPadding(40, 80, 40, 80);
                            empty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                            lectureListContainer.addView(empty);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(
                                LectureListActivity.this,
                                "Error loading lectures",
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        LectureListActivity.this,
                        "Error loading attempt info",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }


}