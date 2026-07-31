package com.example.ikhwa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import androidx.cardview.widget.CardView;


public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = "AdminHomeActivity";

    private ImageButton courseBtn, teacherBtn, studentBtn, notificationBtn, menuBtn;
    private CardView courseCard, teacherCard, studentCard, notificationCard;
    private TextView courseCount, teacherCount, studentCount, notificationCount,studedils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Buttons
        courseBtn = findViewById(R.id.course_btn);
        teacherBtn = findViewById(R.id.teacher_btn);
        studentBtn = findViewById(R.id.student_btn);
        notificationBtn = findViewById(R.id.notification_btn);
        menuBtn = findViewById(R.id.menuButton);
        studedils=findViewById(R.id.student_details);
        // Count TextViews
        courseCount = findViewById(R.id.no_of_courses);
        teacherCount = findViewById(R.id.no_of_teachers);
        studentCount = findViewById(R.id.no_of_students);
        notificationCount = findViewById(R.id.no_of_items);
        courseCard = findViewById(R.id.courseCard);
        teacherCard = findViewById(R.id.teacherCard);
        studentCard = findViewById(R.id.studentCard);
        notificationCard = findViewById(R.id.notificationCard);


        loadCountsFromFirebase();
        setupListeners();
        listenForPendingTeacherRequests();
    }

    private void loadCountsFromFirebase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // Courses Count (from current courses)
        db.child("Courses").child("currentCourse").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalCourses = snapshot.getChildrenCount(); // just count all children under currentCourse
                courseCount.setText(totalCourses + " Courses");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                courseCount.setText("0 Courses");
            }
        });

        // Teachers Count (approved teachers)
        db.child("Teachers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int approvedCount = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String status = child.child("status").getValue(String.class); // get status field
                    if ("approved".equalsIgnoreCase(status)) { // check if status is approved
                        approvedCount++;
                    }
                }

                teacherCount.setText(approvedCount + " Teachers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                teacherCount.setText("0 Teachers");
            }
        });

        // Students Count
        db.child("Student").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                studentCount.setText(count + " Students");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                studentCount.setText("0 Students");
            }
        });

        // Notifications Count
        db.child("Notifications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                notificationCount.setText(count + " Notifications");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                notificationCount.setText("0 Notifications");
            }
        });
    }

    private void setupListeners() {

        View.OnClickListener courseClick = v ->
                startActivity(new Intent(this, CourseActivity.class));

        View.OnClickListener teacherClick = v ->
                startActivity(new Intent(this, ApprovedRejectedTeachersActivity.class));

        View.OnClickListener studentClick = v ->
                startActivity(new Intent(this, Students_Details_Activity.class));

        View.OnClickListener notificationClick = v ->
                startActivity(new Intent(this, AdminNotificationActivity.class));

        // Image Buttons
        courseBtn.setOnClickListener(courseClick);
        teacherBtn.setOnClickListener(teacherClick);
        studentBtn.setOnClickListener(studentClick);
        notificationBtn.setOnClickListener(notificationClick);

        // Full Cards
        courseCard.setOnClickListener(courseClick);
        teacherCard.setOnClickListener(teacherClick);
        studentCard.setOnClickListener(studentClick);
        notificationCard.setOnClickListener(notificationClick);

        studedils.setOnClickListener(v ->
                startActivity(new Intent(this, PendingTeachersActivity.class)));

        menuBtn.setOnClickListener(this::showPopupMenu);
    }

    private void listenForPendingTeacherRequests() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PendingTeacherRequests");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String status = snap.child("status").getValue(String.class);
                    if ("pending".equals(status)) {
                        String name = snap.child("name").getValue(String.class);
                        String key = snap.getKey();
                        showNotificationCard(name, key);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminHomeActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, 0, 0, R.style.PopupMenuStyle);
        popupMenu.getMenuInflater().inflate(R.menu.logout, popupMenu.getMenu());

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem menuItem = popupMenu.getMenu().getItem(i);
            SpannableString spanString = new SpannableString(menuItem.getTitle());
            spanString.setSpan(new ForegroundColorSpan(Color.parseColor("#242E67")), 0, spanString.length(), 0);
            menuItem.setTitle(spanString);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logoutUser();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void logoutUser() {
        Log.d(TAG, "Logging out user");

        SharedPreferences sharedPreferences = getSharedPreferences(SplashActivity.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SplashActivity.ROLE_KEY);
        editor.apply();

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(AdminHomeActivity.this, SelectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Application")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showNotificationCard(String name, String key) {
        Log.d(TAG, "New Pending Request: " + name + " (" + key + ")");
    }
}
