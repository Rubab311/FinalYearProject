package com.example.ikhwa;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class LectureDetailActivity extends AppCompatActivity {


    private String courseId;
    private String lectureId;
    private String userId;


    private TextView tvVideoTitle;
    private TextView tvVideoLink;
    private TextView tvFinalResult;


    private LinearLayout quizContainer;

    private ProgressBar quizProgressBar;

    private Button btnSubmitAll;


    private LayoutInflater inflater;


    private final List<QuizItem> quizItemList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lecture_detail);



        courseId = getIntent().getStringExtra("courseId");

        lectureId = getIntent().getStringExtra("lectureId");



        userId = SessionManager.getCurrentMemberId(this);

        if (userId == null || userId.isEmpty()) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                userId = user.getUid();
            } else {

                Toast.makeText(
                        this,
                        "Please login again",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
                return;
            }
        }




        inflater = LayoutInflater.from(this);



        tvVideoTitle = findViewById(R.id.tvVideoTitle);

        tvVideoLink = findViewById(R.id.tvVideoLink);


        quizContainer = findViewById(R.id.quizContainer);


        quizProgressBar = findViewById(R.id.quizProgressBar);


        tvFinalResult = findViewById(R.id.percentageText);


        btnSubmitAll = findViewById(R.id.btnSubmitAll);



        btnSubmitAll.setOnClickListener(v -> evaluateQuiz());



        loadLectureDetails();

    }





    private void loadLectureDetails() {


        DatabaseReference ref =
                FirebaseDatabase.getInstance()
                        .getReference("Courses")
                        .child(courseId)
                        .child("lectures")
                        .child(lectureId);



        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {


                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {



                        quizItemList.clear();


                        String title =
                                snapshot.child("title")
                                        .getValue(String.class);



                        String videoUrl =
                                snapshot.child("videoUrl")
                                        .getValue(String.class);



                        tvVideoTitle.setText(
                                title != null ? title : "Lecture"
                        );



                        if(videoUrl != null &&
                                !videoUrl.isEmpty()){


                            tvVideoLink.setText(
                                    Html.fromHtml(
                                            "<u>" + videoUrl + "</u>"
                                    )
                            );


                            tvVideoLink.setTextColor(
                                    Color.parseColor("#2962FF")
                            );



                            tvVideoLink.setOnClickListener(v -> {


                                Intent intent =
                                        new Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(videoUrl)
                                        );


                                startActivity(intent);


                            });


                        }
                        else{


                            tvVideoLink.setText(
                                    "Video not available"
                            );


                            tvVideoLink.setTextColor(Color.RED);

                        }



                        quizContainer.removeAllViews();



                        DataSnapshot quizzes =
                                snapshot.child("quizzes");



                        int questionNumber = 1;
                        for (DataSnapshot quizSnap : quizzes.getChildren()) {


                            String question =
                                    quizSnap.child("question")
                                            .getValue(String.class);



                            String correctAnswer =
                                    quizSnap.child("correctAnswer")
                                            .getValue(String.class);



                            List<String> options =
                                    new ArrayList<>();


                            for(DataSnapshot option :
                                    quizSnap.child("options").getChildren()){


                                String optionText =
                                        option.getValue(String.class);


                                if(optionText != null){

                                    options.add(optionText);

                                }

                            }




                            View quizCard =
                                    inflater.inflate(
                                            R.layout.item_quiz_card,
                                            quizContainer,
                                            false
                                    );



                            TextView tvQuestionNo =
                                    quizCard.findViewById(
                                            R.id.tvQuestionNo
                                    );


                            TextView tvQuestion =
                                    quizCard.findViewById(
                                            R.id.tvQuestion
                                    );


                            RadioGroup radioGroup =
                                    quizCard.findViewById(
                                            R.id.radioGroup
                                    );


                            TextView tvResult =
                                    quizCard.findViewById(
                                            R.id.tvResult
                                    );




                            tvQuestionNo.setText(
                                    String.valueOf(questionNumber)
                            );


                            tvQuestion.setText(
                                    question != null ?
                                            question :
                                            "Question"
                            );



                            radioGroup.removeAllViews();




                            for(String option : options){


                                RadioButton rb =
                                        new RadioButton(
                                                LectureDetailActivity.this
                                        );


                                rb.setText(option);

                                rb.setTextSize(16);

                                rb.setTextColor(Color.BLACK);

                                rb.setPadding(
                                        10,
                                        12,
                                        10,
                                        12
                                );



                                radioGroup.addView(rb);


                            }




                            quizContainer.addView(quizCard);




                            quizItemList.add(
                                    new QuizItem(
                                            radioGroup,
                                            correctAnswer,
                                            tvResult
                                    )
                            );



                            questionNumber++;

                        }



                        if(quizItemList.isEmpty()){


                            Toast.makeText(
                                    LectureDetailActivity.this,
                                    "No quiz available",
                                    Toast.LENGTH_SHORT
                            ).show();


                        }



                    }



                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {


                        Toast.makeText(
                                LectureDetailActivity.this,
                                "Failed to load lecture",
                                Toast.LENGTH_SHORT
                        ).show();

                    }


                });


    }





    private void evaluateQuiz(){


        int total =
                quizItemList.size();



        if(total == 0){


            Toast.makeText(
                    this,
                    "No quiz available",
                    Toast.LENGTH_SHORT
            ).show();


            return;

        }




        int correct = 0;



        for(QuizItem item : quizItemList){



            int selectedId =
                    item.radioGroup
                            .getCheckedRadioButtonId();




            if(selectedId == -1){



                item.resultView.setText(
                        "❌ Not Answered"
                );


                item.resultView.setTextColor(
                        Color.RED
                );



            }
            else{


                RadioButton selected =
                        item.radioGroup
                                .findViewById(selectedId);



                String answer =
                        selected.getText()
                                .toString();




                if(answer.equals(
                        item.correctAnswer)){


                    correct++;


                    item.resultView.setText(
                            "✅ Correct"
                    );


                    item.resultView.setTextColor(
                            Color.parseColor("#2E7D32")
                    );


                }
                else{


                    item.resultView.setText(
                            "❌ Wrong\nCorrect Answer : "
                                    + item.correctAnswer
                    );


                    item.resultView.setTextColor(
                            Color.RED
                    );


                }


            }



            for(int i=0;
                i<item.radioGroup.getChildCount();
                i++){


                item.radioGroup
                        .getChildAt(i)
                        .setEnabled(false);


            }


            item.resultView.setVisibility(View.VISIBLE);


        }
        int percentage =
                (int) ((correct * 100f) / total);



        quizProgressBar.setProgress(
                percentage
        );


        tvFinalResult.setText(
                percentage + "%"
        );



        saveLectureAttempt();



    }





    private void saveLectureAttempt(){



        if(userId == null ||
                courseId == null ||
                lectureId == null){


            Toast.makeText(
                    this,
                    "Unable to save attempt",
                    Toast.LENGTH_SHORT
            ).show();


            return;

        }




        DatabaseReference attemptRef =
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(userId)
                        .child("attemptedLectures")
                        .child(courseId)
                        .child(lectureId);




        attemptRef.setValue(true)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            LectureDetailActivity.this,
                            "✅ Quiz Submitted Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);

                    finish();

                })
                .addOnFailureListener(e -> {



                    Toast.makeText(
                            LectureDetailActivity.this,
                            "Failed to save attempt",
                            Toast.LENGTH_SHORT
                    ).show();



                });



    }





    private static class QuizItem{


        RadioGroup radioGroup;

        String correctAnswer;

        TextView resultView;




        QuizItem(
                RadioGroup radioGroup,
                String correctAnswer,
                TextView resultView
        ){


            this.radioGroup = radioGroup;

            this.correctAnswer = correctAnswer;

            this.resultView = resultView;


        }


    }



}