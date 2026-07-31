package com.example.ikhwa;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ikhwa.modules.Course;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentHome2 extends AppCompatActivity {

    Dialog myDialog, myDialog2;

    TextView studentNameText;
    TextView studentEmailText;
    TextView notificationBadge;

    DatabaseReference databaseReference;

    FirebaseAuth mAuth;

    LinearLayout layout;

    RecyclerView courseRecycler;

    List<Course> courseList;

    CourseAdapter courseAdapter;

    Button notifybtn;

    String currentMemberId;

    int[] backgroundColors = {
            R.drawable.bg_green,
            R.drawable.bg_blue,
            R.drawable.bg_red
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_home2);

        mAuth = FirebaseAuth.getInstance();

        // Selected Family Member
        currentMemberId = SessionManager.getCurrentMemberId(this);

        if (currentMemberId == null || currentMemberId.isEmpty()) {
            currentMemberId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (currentMemberId == null || currentMemberId.isEmpty()) {
            currentMemberId = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();
        }

        studentNameText = findViewById(R.id.st_name);
        studentEmailText = findViewById(R.id.st_email);
        notificationBadge = findViewById(R.id.notification_badge);

        layout = findViewById(R.id.st_add_your);

        notifybtn = findViewById(R.id.notification_click_to_open);

        courseRecycler = findViewById(R.id.recyclerCourseContainer);

        courseRecycler.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false));

        courseList = new ArrayList<>();

        courseAdapter = new CourseAdapter(this, courseList);

        courseRecycler.setAdapter(courseAdapter);

        notifybtn.setOnClickListener(v -> {

            DatabaseReference notifRef =
                    FirebaseDatabase.getInstance()
                            .getReference("Notifications");

            DatabaseReference readRef =
                    FirebaseDatabase.getInstance()
                            .getReference("NotificationReads")
                            .child("Student")
                            .child(currentMemberId);

            notifRef.get().addOnSuccessListener(snapshot -> {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    readRef.child(ds.getKey()).setValue(true);
                }

                notificationBadge.setVisibility(View.GONE);

                startActivity(new Intent(
                        StudentHome2.this,
                        StudentNotificationActivity.class));

            });

        });

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Student")
                .child(currentMemberId);

        databaseReference.get().addOnSuccessListener(snapshot -> {

            if (snapshot.exists()) {

                studentNameText.setText(
                        snapshot.child("student_name")
                                .getValue(String.class));

                studentEmailText.setText(
                        snapshot.child("email")
                                .getValue(String.class));

                showEnrolledCourses(currentMemberId);

            }

        });

        myDialog = new Dialog(this);
        myDialog2 = new Dialog(this);

        BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_profile) {

                startActivity(new Intent(
                        this,
                        stdprofile.class));

                return true;

            } else if (item.getItemId() == R.id.nav_setting) {

                startActivity(new Intent(
                        this,
                        SettingActivity.class));

                return true;

            }

            return true;
        });

        loadCurrentCourses();

        checkUnreadNotifications();

    }
    private void checkUnreadNotifications() {

        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("Notifications");

        DatabaseReference readRef = FirebaseDatabase.getInstance()
                .getReference("NotificationReads")
                .child("Student")
                .child(currentMemberId);

        notifRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot allNotifsSnapshot) {

                readRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot readSnapshot) {

                        int unreadCount = 0;

                        for (DataSnapshot notif : allNotifsSnapshot.getChildren()) {

                            String target =
                                    notif.child("target").getValue(String.class);

                            if (target != null &&
                                    (target.equals("Student")
                                            || target.equalsIgnoreCase("all"))) {

                                if (!readSnapshot.hasChild(notif.getKey())) {

                                    unreadCount++;

                                }

                            }

                        }

                        if (unreadCount > 0) {

                            notificationBadge.setVisibility(View.VISIBLE);

                            notificationBadge.setText(
                                    String.valueOf(unreadCount));

                        } else {

                            notificationBadge.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }



    private void loadCurrentCourses() {

        DatabaseReference courseRef = FirebaseDatabase.getInstance()
                .getReference("Courses")
                .child("currentCourse");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                courseList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Course course = ds.getValue(Course.class);

                    if (course != null) {

                        course.setId(ds.getKey());

                        courseList.add(course);

                    }

                }

                courseAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        StudentHome2.this,
                        "Unable to load courses",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }
    private void showEnrolledCourses(String memberId) {

        layout.removeAllViews();

        DatabaseReference enrollRef = FirebaseDatabase.getInstance()
                .getReference("Enrollments")
                .child(memberId);

        DatabaseReference courseRef = FirebaseDatabase.getInstance()
                .getReference("Courses")
                .child("currentCourse");

        enrollRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot enrollSnapshot) {

                if (!enrollSnapshot.exists()) {

                    TextView txt = new TextView(StudentHome2.this);

                    txt.setText("No enrolled courses");
                    txt.setTextSize(18);

                    layout.addView(txt);

                    return;
                }

                for (DataSnapshot ds : enrollSnapshot.getChildren()) {

                    String courseId = ds.getKey();

                    courseRef.child(courseId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()) {

                                        Course course =
                                                snapshot.getValue(Course.class);

                                        if (course != null) {

                                            course.setId(courseId);

                                            if (snapshot.hasChild("groups")) {

                                                for (DataSnapshot group :
                                                        snapshot.child("groups").getChildren()) {

                                                    course.setGroupId(group.getKey());

                                                    break;
                                                }

                                            }

                                            addCourseCard(course, memberId);

                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }

                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        StudentHome2.this,
                        "Failed to load enrolled courses",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }
    private void addCourseCard(Course course, String memberId) {

        View courseView = getLayoutInflater()
                .inflate(R.layout.your_course_card, null);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(16, 0, 16, 0);

        courseView.setLayoutParams(params);

        TextView tvCourseTitle =
                courseView.findViewById(R.id.tv_course_title);

        TextView tvChapterCount =
                courseView.findViewById(R.id.tv_chapter_count);

        TextView tvType =
                courseView.findViewById(R.id.course_type);

        TextView tvProgress =
                courseView.findViewById(R.id.tv_progress);

        ProgressBar progressBar =
                courseView.findViewById(R.id.progress_bar);

        RelativeLayout cardLayout =
                courseView.findViewById(R.id.card_background_layout);

        Button btnView =
                courseView.findViewById(R.id.btn_view_lecture);


        tvCourseTitle.setText(course.getTitle());
        tvChapterCount.setText(course.getDuration());
        tvType.setText(course.getType());

        int index = layout.getChildCount();

        cardLayout.setBackgroundResource(
                backgroundColors[index % backgroundColors.length]
        );


        // ===============================
        // QUIZ BASED COURSE
        // ===============================

        if (course.getType().equalsIgnoreCase("Quiz Based")) {

            DatabaseReference lectureRef =
                    FirebaseDatabase.getInstance()
                            .getReference("Courses")
                            .child(course.getId())
                            .child("lectures");

            DatabaseReference attemptRef =
                    FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(memberId)
                            .child("attemptedLectures")
                            .child(course.getId());

            lectureRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(
                                @NonNull DataSnapshot lectureSnap) {

                            long total =
                                    lectureSnap.getChildrenCount();

                            attemptRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {

                                        @Override
                                        public void onDataChange(
                                                @NonNull DataSnapshot attemptSnap) {

                                            long attempted =
                                                    attemptSnap.getChildrenCount();

                                            tvProgress.setText(
                                                    attempted + "/" + total +
                                                            " Completed");

                                            if (total > 0) {

                                                progressBar.setProgress(
                                                        (int) ((attempted * 100) / total));

                                            }

                                        }

                                        @Override
                                        public void onCancelled(
                                                @NonNull DatabaseError error) {

                                        }

                                    });

                        }

                        @Override
                        public void onCancelled(
                                @NonNull DatabaseError error) {

                        }

                    });


            btnView.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                StudentHome2.this,
                                LectureListActivity.class);

                intent.putExtra("courseId", course.getId());

                startActivity(intent);

            });

        }
        // ===============================
        // ATTENDANCE BASED COURSE
        // ===============================

        else if (course.getType().equalsIgnoreCase("Attendance Based")) {

            DatabaseReference attendanceRef =
                    FirebaseDatabase.getInstance()
                            .getReference("Attendance")
                            .child(course.getId())
                            .child(course.getGroupId());

            attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    int present = 0;
                    int total = 0;

                    for (DataSnapshot dateSnap : snapshot.getChildren()) {

                        String status =
                                dateSnap.child(memberId).getValue(String.class);

                        if (status != null) {

                            total++;

                            if (status.equals("P")) {
                                present++;
                            }

                        }

                    }

                    tvProgress.setText(
                            present + "/" + total + " Present"
                    );

                    if (total > 0) {

                        int percent =
                                (int) ((present * 100.0f) / total);

                        progressBar.setProgress(percent);

                    } else {

                        progressBar.setProgress(0);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });

            btnView.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                StudentHome2.this,
                                std_crs_att.class
                        );

                intent.putExtra("courseId", course.getId());
                intent.putExtra("groupId", course.getGroupId());
                intent.putExtra("courseTitle", course.getTitle());

                startActivity(intent);

            });

        }

        layout.addView(courseView);

    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Exit Application")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    finishAffinity();

                })
                .setNegativeButton("No", (dialog, which) -> {

                    dialog.dismiss();

                })
                .create()
                .show();

    }

}