package com.example.ikhwa;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.ikhwa.modules.TeacherDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class Teacher_home extends AppCompatActivity {

    Button t_staff_see_more, t_course_see_more, btn_see_more_pro;
    TextView tv_notification, tv_setting, badgeTextView, tvEmailTea, tvNameTea;

    List<TeacherDetails> teacherList = new ArrayList<>();
    int unseenCount = 0;

    DatabaseReference notifRef, readRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_home);

        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish(); // not logged in
            return;
        }

        // UI Bindings
        t_staff_see_more = findViewById(R.id.tbtn_see_staff);
        t_course_see_more = findViewById(R.id.tbtn_see_course);
        tv_notification = findViewById(R.id.bell_icon_tea);
        btn_see_more_pro = findViewById(R.id.btn_see_moretea);
        tv_setting = findViewById(R.id.tea_setting);
        badgeTextView = findViewById(R.id.notification_badge);
        tvEmailTea = findViewById(R.id.tv_email_tea);
        tvNameTea = findViewById(R.id.tv_name_tea); // <- Add this line

        // Set email
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email != null) {
            tvEmailTea.setText(email);
        }

        // Navigation
        tv_setting.setOnClickListener(v -> startActivity(new Intent(this, TeacherSettingActivity.class)));
        btn_see_more_pro.setOnClickListener(v -> startActivity(new Intent(this, TeacherProfileActivity.class)));
        t_course_see_more.setOnClickListener(v -> startActivity(new Intent(this, TeacherCourseActivity.class)));
        t_staff_see_more.setOnClickListener(v -> startActivity(new Intent(this, StafftActivity.class)));

        // Notification
        tv_notification.setOnClickListener(v -> {
            startActivity(new Intent(Teacher_home.this, TeacherNotificationActivity.class));
            badgeTextView.setVisibility(View.GONE);
        });

        // Notifications and Data
        checkUnreadNotifications();
        loadTeacherData(); // <- Load name from TeacherRequests
        CardView cardAboutInstitute = findViewById(R.id.cardAboutInstitute);

        cardAboutInstitute.setOnClickListener(v -> {
            Intent intent = new Intent(Teacher_home.this, AboutUsActivity.class);
            startActivity(intent);
        });
    }

    private void checkUnreadNotifications() {
        notifRef = FirebaseDatabase.getInstance().getReference("Notifications");
        readRef = FirebaseDatabase.getInstance().getReference("NotificationReads")
                .child("Teacher").child(uid);

        notifRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot allNotifsSnapshot) {
                readRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot readSnapshot) {
                        unseenCount = 0;

                        for (DataSnapshot notif : allNotifsSnapshot.getChildren()) {
                            String notifKey = notif.getKey();
                            String target = notif.child("target").getValue(String.class);

                            if (target != null &&
                                    (target.equalsIgnoreCase("Teacher") || target.equalsIgnoreCase("All")) &&
                                    !readSnapshot.hasChild(notifKey)) {
                                unseenCount++;
                            }
                        }

                        updateBadge();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateBadge() {
        if (badgeTextView != null) {
            if (unseenCount > 0) {
                badgeTextView.setText(String.valueOf(unseenCount));
                badgeTextView.setVisibility(View.VISIBLE);
            } else {
                badgeTextView.setVisibility(View.GONE);
            }
        }
    }

    private void loadTeacherData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Teachers");

        ref.orderByChild("email").equalTo(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            TeacherDetails details = snap.getValue(TeacherDetails.class);
                            if (details != null && details.getName() != null) {
                                tvNameTea.setText(details.getName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

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
}
