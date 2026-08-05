package com.example.ikhwa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class TeacherGroupsActivity extends AppCompatActivity {

    DatabaseReference coursesRef;
    String teacherUid, courseId, courseTitle;
    LinearLayout groupsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_groups);

        groupsContainer = findViewById(R.id.groupsContainer);
        teacherUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get course data from intent
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");

        coursesRef = FirebaseDatabase.getInstance().getReference("Courses/currentCourse").child(courseId);

        loadAssignedGroups();
    }

    private void loadAssignedGroups() {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot courseSnap) {
                groupsContainer.removeAllViews();

                if (!courseSnap.exists()) return;

                if (courseSnap.hasChild("groups")) {
                    DataSnapshot groupsSnap = courseSnap.child("groups");

                    for (DataSnapshot groupSnap : groupsSnap.getChildren()) {
                        String groupId = groupSnap.getKey();
                        String assignedId = groupSnap.child("assignedTeacherId").getValue(String.class);

                        if (teacherUid.equals(assignedId)) {
                            // Create group card layout
                            LinearLayout cardLayout = new LinearLayout(TeacherGroupsActivity.this);
                            cardLayout.setOrientation(LinearLayout.VERTICAL);
                            cardLayout.setBackgroundResource(R.drawable.rounded_card);
                            cardLayout.setPadding(32, 32, 32, 32);

                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            cardParams.setMargins(0, 0, 0, 30);
                            cardLayout.setLayoutParams(cardParams);

                            // Course Title
                            TextView courseTitleView = new TextView(TeacherGroupsActivity.this);
                            courseTitleView.setText("📘 Course: " + courseTitle);
                            courseTitleView.setTextSize(20f);
                            courseTitleView.setTextColor(getResources().getColor(android.R.color.black));
                            courseTitleView.setPadding(0, 0, 0, 10);

                            // Group ID
                            TextView groupNameView = new TextView(TeacherGroupsActivity.this);
                            groupNameView.setText("👥 Group: " + groupId);
                            groupNameView.setTextSize(16f);
                            groupNameView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            groupNameView.setPadding(0, 0, 0, 20);

                            // Student list container
                            LinearLayout studentListLayout = new LinearLayout(TeacherGroupsActivity.this);
                            studentListLayout.setOrientation(LinearLayout.VERTICAL);

                            // Load student UIDs
                            DataSnapshot studentListSnap = groupSnap.child("students");
                            if (studentListSnap.exists()) {
                                for (DataSnapshot studentUidSnap : studentListSnap.getChildren()) {
                                    String studentUid = studentUidSnap.getKey();
                                    loadStudentDetailsInto(studentUid, groupId, studentListLayout);                                }
                            } else {
                                TextView noStudents = new TextView(TeacherGroupsActivity.this);
                                noStudents.setText("🚫 No students in this group.");
                                noStudents.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                studentListLayout.addView(noStudents);
                            }

                            // Attendance Button
                            Button markAttendanceBtn = new Button(TeacherGroupsActivity.this);
                            markAttendanceBtn.setText("Mark Attendance");
                            markAttendanceBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                            markAttendanceBtn.setTextColor(getResources().getColor(android.R.color.white));
                            markAttendanceBtn.setPadding(20, 10, 20, 10);

                            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            btnParams.setMargins(0, 30, 0, 0);
                            markAttendanceBtn.setLayoutParams(btnParams);

                            markAttendanceBtn.setOnClickListener(v -> {
                                Intent intent = new Intent(TeacherGroupsActivity.this, TeacherAttendanceActivity.class);
                                intent.putExtra("courseId", courseId);
                                intent.putExtra("groupId", groupId);
                                intent.putExtra("courseTitle", courseTitle);
                                startActivity(intent);
                            });

                            // Listen Lesson Button (Inside same card)
                            Button listenLessonBtn = new Button(TeacherGroupsActivity.this);
                            listenLessonBtn.setText("Listen Lesson");
                            listenLessonBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                            listenLessonBtn.setTextColor(getResources().getColor(android.R.color.white));
                            listenLessonBtn.setPadding(20, 10, 20, 10);

                            LinearLayout.LayoutParams listenBtnParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            listenBtnParams.setMargins(0, 20, 0, 0);
                            listenLessonBtn.setLayoutParams(listenBtnParams);

                            // WhatsApp Group Link
                            String groupLink = "https://chat.whatsapp.com"; // Apna link yahan dalain

                            listenLessonBtn.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(android.net.Uri.parse(groupLink));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(TeacherGroupsActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Add views to card
                            cardLayout.addView(courseTitleView);
                            cardLayout.addView(groupNameView);
                            cardLayout.addView(studentListLayout);
                            cardLayout.addView(markAttendanceBtn);
                            cardLayout.addView(listenLessonBtn); // Added inside card

                            // Add card to container
                            groupsContainer.addView(cardLayout);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", error.getMessage());
            }
        });
    }

    private void loadStudentDetailsInto(String uid,
                                        String groupId,
                                        LinearLayout studentListLayout) {
        DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("Student");

        studentsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String name = snapshot.child("student_name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);

                TextView studentView = new TextView(TeacherGroupsActivity.this);
                studentView.setText("👤 " + name + "\n" + email + "\n" + phone);
                studentView.setTextSize(15f);
                studentView.setPadding(20, 10, 20, 10);
                studentView.setTextColor(getResources().getColor(android.R.color.black));
                studentView.setBackgroundResource(android.R.drawable.editbox_background);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 5, 0, 5);
                studentView.setLayoutParams(params);
                studentView.setClickable(true);
                studentView.setFocusable(true);

                studentView.setOnClickListener(v -> {

                    Intent intent = new Intent(TeacherGroupsActivity.this,
                            StudentProgressActivity.class);

                    intent.putExtra("studentUid", uid);
                    intent.putExtra("courseId", courseId);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("courseTitle", courseTitle);

                    startActivity(intent);

                });
                studentListLayout.addView(studentView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
