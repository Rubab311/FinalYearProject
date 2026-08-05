package com.example.ikhwa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class StudentProgressActivity extends AppCompatActivity {


    private TextView tvStudentName;
    private TextView tvAttendancePercent;
    private TextView tvPresent;
    private TextView tvAbsent;
    private TextView tvLeave;

    private ProgressBar progressAttendance;

    private LinearLayout historyContainer;


    private String studentUid;
    private String courseId;
    private String groupId;
    private String courseType;


    private DatabaseReference studentRef;
    private DatabaseReference attendanceRef;


    private int present = 0;
    private int absent = 0;
    private int leave = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_progress);


        tvStudentName = findViewById(R.id.tvStudentName);
        tvAttendancePercent = findViewById(R.id.tvAttendancePercent);

        tvPresent = findViewById(R.id.tvPresent);
        tvAbsent = findViewById(R.id.tvAbsent);
        tvLeave = findViewById(R.id.tvLeave);

        progressAttendance = findViewById(R.id.progressAttendance);

        historyContainer = findViewById(R.id.historyContainer);



        studentUid = getIntent().getStringExtra("studentUid");
        courseId = getIntent().getStringExtra("courseId");
        groupId = getIntent().getStringExtra("groupId");
        courseType = getIntent().getStringExtra("courseType");



        studentRef = FirebaseDatabase.getInstance()
                .getReference("Student")
                .child(studentUid);



        loadStudentName();



        if("Quiz Based".equalsIgnoreCase(courseType)){

            loadQuizProgress();

        }else{


            attendanceRef = FirebaseDatabase.getInstance()
                    .getReference("Attendance")
                    .child(courseId)
                    .child(groupId);


            loadAttendance();

        }

    }




    private void loadStudentName() {


        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                String name =
                        snapshot.child("student_name")
                                .getValue(String.class);


                if(name != null){

                    tvStudentName.setText(name);

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }






    // ==============================
    // ATTENDANCE BASED
    // ==============================

    private void loadAttendance() {


        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                present = 0;
                absent = 0;
                leave = 0;


                historyContainer.removeAllViews();



                for(DataSnapshot dateSnap : snapshot.getChildren()){


                    String date =
                            dateSnap.getKey();



                    String status =
                            dateSnap.child(studentUid)
                                    .getValue(String.class);



                    if(status == null)
                        continue;



                    switch(status){

                        case "P":
                            present++;
                            break;


                        case "A":
                            absent++;
                            break;


                        case "L":
                            leave++;
                            break;

                    }


                    addHistoryItem(date,status);


                }



                updateAttendanceSummary();



            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }






    private void updateAttendanceSummary(){


        tvPresent.setText(String.valueOf(present));

        tvAbsent.setText(String.valueOf(absent));

        tvLeave.setText(String.valueOf(leave));



        int total =
                present + absent + leave;



        double percentage = 0;



        if(total > 0){

            percentage =
                    (present * 100.0) / total;

        }



        tvAttendancePercent.setText(
                String.format(
                        Locale.getDefault(),
                        "Attendance : %.1f%%",
                        percentage
                )
        );


        progressAttendance.setProgress(
                (int)percentage
        );

    }







    // ==============================
    // QUIZ BASED
    // ==============================

    private void loadQuizProgress(){


        DatabaseReference quizRef =
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(studentUid)
                        .child("attemptedLectures")
                        .child(courseId);



        quizRef.addListenerForSingleValueEvent(
                new ValueEventListener() {


                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        historyContainer.setVisibility(View.GONE);

                        int attempted =
                                (int)snapshot.getChildrenCount();



                        tvAttendancePercent.setText(
                                "Quiz Completed : " + attempted
                        );



                        tvPresent.setText(
                                String.valueOf(attempted)
                        );


                        tvAbsent.setText("0");

                        tvLeave.setText("0");



                        progressAttendance.setProgress(100);




                    }


                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {


                    }

                });


    }



    private void addHistoryItem(
            String date,
            String status){



        TextView item =
                new TextView(this);



        String text = "";



        if(status.equals("P")){


            text =
                    "📅 " + date + "   ✅ Present";


            item.setTextColor(
                    Color.parseColor("#2E7D32")
            );


        }
        else if(status.equals("A")){


            text =
                    "📅 " + date + "   ❌ Absent";


            item.setTextColor(
                    Color.parseColor("#C62828")
            );


        }
        else if(status.equals("L")){


            text =
                    "📅 " + date + "   🟡 Leave";


            item.setTextColor(
                    Color.parseColor("#F9A825")
            );

        }



        item.setText(text);

        item.setTextSize(16);

        item.setGravity(Gravity.CENTER_VERTICAL);


        item.setPadding(
                20,
                20,
                20,
                20
        );


        historyContainer.addView(item);

    }

}