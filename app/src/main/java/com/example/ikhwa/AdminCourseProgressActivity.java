package com.example.ikhwa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AdminCourseProgressActivity extends AppCompatActivity {


    private LinearLayout studentsContainer;


    private DatabaseReference courseRef;
    private DatabaseReference studentRef;
    private DatabaseReference attendanceRef;
    private DatabaseReference usersRef;


    private String courseId;
    private String courseType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_course_progress);



        studentsContainer =
                findViewById(R.id.studentsContainer);



        courseId = getIntent().getStringExtra("courseId");
        courseType = getIntent().getStringExtra("courseType");


        courseRef =
                FirebaseDatabase.getInstance()
                        .getReference("Courses")
                        .child("currentCourse")
                        .child(courseId);



        studentRef =
                FirebaseDatabase.getInstance()
                        .getReference("Student");



        attendanceRef =
                FirebaseDatabase.getInstance()
                        .getReference("Attendance")
                        .child(courseId);



        usersRef =
                FirebaseDatabase.getInstance()
                        .getReference("Users");



        if(courseType != null){

            loadStudents();

        }else{

            getCourseType();

        }
        if(courseId == null){
            finish();
            return;
        }

    }





    private void getCourseType(){


        courseRef.child("type")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {


                                courseType =
                                        snapshot.getValue(String.class);


                                loadStudents();

                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }

                        });


    }







    private void loadStudents(){

        if("Quiz Based".equalsIgnoreCase(courseType)){

            DatabaseReference enrollRef =
                    FirebaseDatabase.getInstance()
                            .getReference("Enrollments");


            enrollRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    studentsContainer.removeAllViews();


                    for(DataSnapshot studentSnap : snapshot.getChildren()){

                        String uid = studentSnap.getKey();


                        if(studentSnap.hasChild(courseId)){

                            loadStudent(uid, "");

                        }

                    }

                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });


        }else{

            courseRef.child("groups")
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            studentsContainer.removeAllViews();

                            for(DataSnapshot groupSnap : snapshot.getChildren()){

                                String groupId = groupSnap.getKey();

                                for(DataSnapshot studentSnap :
                                        groupSnap.child("students").getChildren()){


                                    String uid = studentSnap.getKey();

                                    if(uid != null){
                                        loadStudent(uid,groupId);
                                    }

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }




    private void loadStudent(
            String uid,
            String groupId){



        studentRef.child(uid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {


                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {



                                String name =
                                        snapshot.child("student_name")
                                                .getValue(String.class);



                                createStudentCard(
                                        uid,
                                        groupId,
                                        name != null ?
                                                name :
                                                "Unknown"
                                );


                            }



                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                            }

                        });


    }









    private void createStudentCard(
            String uid,
            String groupId,
            String name){



        LinearLayout card =
                new LinearLayout(this);



        card.setOrientation(
                LinearLayout.VERTICAL
        );



        card.setPadding(10, 5, 10, 5);


        card.setBackgroundResource(
                R.drawable.rounded_card
        );



        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );



        params.setMargins(
                0,
                10,
                0,
                10
        );



        card.setLayoutParams(params);



        TextView nameText =
                new TextView(this);


        nameText.setText(
                "👤 " + name
        );


        nameText.setTextSize(18);


        nameText.setTextColor(
                Color.BLACK
        );



        TextView progressText =
                new TextView(this);



        progressText.setTextSize(16);



        TextView statusText =
                new TextView(this);



        statusText.setTextSize(16);



        card.addView(nameText);

        card.addView(progressText);

        card.addView(statusText);



        studentsContainer.addView(card);





        if("Quiz Based".equalsIgnoreCase(courseType)){


            checkQuizCompletion(
                    uid,
                    progressText,
                    statusText
            );


        }
        else{


            calculateAttendance(
                    uid,
                    progressText,
                    statusText
            );

        }





        card.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AdminCourseProgressActivity.this,
                            StudentProgressActivity.class
                    );


            intent.putExtra(
                    "studentUid",
                    uid
            );


            intent.putExtra(
                    "courseId",
                    courseId
            );


            intent.putExtra(
                    "groupId",
                    groupId
            );


            intent.putExtra(
                    "courseType",
                    courseType
            );


            startActivity(intent);


        });



    }









    private void calculateAttendance(
            String uid,
            TextView progress,
            TextView status){



        attendanceRef.addListenerForSingleValueEvent(
                new ValueEventListener() {


                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {


                        int total = 0;

                        int present = 0;



                        for(DataSnapshot date :
                                snapshot.getChildren()){


                            String value =
                                    date.child(uid)
                                            .getValue(String.class);



                            if(value != null){


                                total++;


                                if(value.equals("P")){

                                    present++;

                                }

                            }


                        }



                        double percent = 0;


                        if(total > 0){

                            percent =
                                    present * 100.0 / total;

                        }



                        progress.setText(
                                String.format(
                                        "Attendance: %.1f%%",
                                        percent
                                )
                        );



                        status.setText(
                                "Status: Active"
                        );

                    }



                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                    }

                });


    }









    private void checkQuizCompletion(
            String uid,
            TextView progress,
            TextView status){



        courseRef.child("lectures")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {


                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot lectureSnap) {



                                int totalLectures =
                                        (int) lectureSnap.getChildrenCount();



                                usersRef.child(uid)
                                        .child("attemptedLectures")
                                        .child(courseId)
                                        .addListenerForSingleValueEvent(
                                                new ValueEventListener() {


                                                    @Override
                                                    public void onDataChange(
                                                            @NonNull DataSnapshot attemptSnap) {



                                                        int attempted =
                                                                (int) attemptSnap.getChildrenCount();



                                                        progress.setText(
                                                                "Quiz Attempt: "
                                                                        + attempted
                                                                        + " / "
                                                                        + totalLectures
                                                        );



                                                        if(totalLectures > 0 &&
                                                                attempted >= totalLectures){


                                                            status.setText(
                                                                    "Status: Completed ✅"
                                                            );


                                                            status.setTextColor(
                                                                    Color.parseColor(
                                                                            "#2E7D32"
                                                                    )
                                                            );


                                                        }
                                                        else{


                                                            status.setText(
                                                                    "Status: In Progress"
                                                            );


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


    }


}