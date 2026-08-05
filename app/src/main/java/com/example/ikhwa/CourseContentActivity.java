package com.example.ikhwa;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CourseContentActivity extends AppCompatActivity {

    private LinearLayout contentLayout;
    private FloatingActionButton btnaddvideoquiz;

    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_content);

        contentLayout = findViewById(R.id.contentLayout);
        btnaddvideoquiz = findViewById(R.id.btnaddvideoquiz);

        courseId = getIntent().getStringExtra("courseId");

        if (courseId != null && !courseId.isEmpty()) {
            loadLecturesWithQuizzes();
        }

        btnaddvideoquiz.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CourseContentActivity.this,
                    AddVideoQuizActivity.class);

            intent.putExtra("courseId", courseId);

            startActivity(intent);

        });

    }

    private interface OnValueSubmitted {
        void onSubmit(String value);
    }
    private void loadLecturesWithQuizzes() {

        DatabaseReference lectureRef = FirebaseDatabase.getInstance()
                .getReference("Courses")
                .child(courseId)
                .child("lectures");


        lectureRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                contentLayout.removeAllViews();

                int lectureNumber = 1;


                for (DataSnapshot lectureSnap : snapshot.getChildren()) {

                    addLectureCard(
                            lectureSnap,
                            lectureRef,
                            lectureNumber
                    );

                    lectureNumber++;

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        CourseContentActivity.this,
                        "Failed to load content",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }
    private void addLectureCard(DataSnapshot lectureSnap,
                                DatabaseReference lectureRef,
                                int lectureNumber) {


        View lectureView = LayoutInflater.from(this)
                .inflate(
                        R.layout.item_lecture,
                        contentLayout,
                        false
                );


        TextView txtLectureTitle =
                lectureView.findViewById(R.id.txtLectureTitle);

        TextView txtLectureName =
                lectureView.findViewById(R.id.txtLectureName);

        TextView txtWatchVideo =
                lectureView.findViewById(R.id.txtWatchVideo);


        ImageButton btnEditLecture =
                lectureView.findViewById(R.id.btnEditLecture);

        ImageButton btnDeleteLecture =
                lectureView.findViewById(R.id.btnDeleteLecture);


        LinearLayout quizContainer =
                lectureView.findViewById(R.id.quizContainer);



        String lectureId = lectureSnap.getKey();


        String title =
                lectureSnap.child("title")
                        .getValue(String.class);


        String videoUrl =
                lectureSnap.child("videoUrl")
                        .getValue(String.class);



        txtLectureTitle.setText(
                "🎥 Lecture " + lectureNumber
        );


        txtLectureName.setText(title);



        // Watch Video

        txtWatchVideo.setOnClickListener(v -> {

            if(videoUrl != null && !videoUrl.isEmpty()){

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(videoUrl)
                );

                startActivity(intent);

            }

        });



        // Edit Lecture

        btnEditLecture.setOnClickListener(v -> {


            showEditDialog(
                    "Edit Lecture Title",
                    title,
                    newValue -> {


                        lectureRef.child(lectureId)
                                .child("title")
                                .setValue(newValue);


                        loadLecturesWithQuizzes();

                    }
            );


        });



        // Delete Lecture

        btnDeleteLecture.setOnClickListener(v -> {


            new AlertDialog.Builder(this)

                    .setTitle("Delete Lecture?")

                    .setMessage(
                            "Are you sure you want to delete this lecture?"
                    )

                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {


                                lectureRef.child(lectureId)
                                        .removeValue();


                                loadLecturesWithQuizzes();


                            })

                    .setNegativeButton(
                            "Cancel",
                            null
                    )

                    .show();


        });



        // Load quizzes

        DataSnapshot quizzes =
                lectureSnap.child("quizzes");


        for(DataSnapshot quizSnap : quizzes.getChildren()){


            addQuizCard(
                    quizSnap,
                    lectureRef,
                    lectureId,
                    quizContainer
            );


        }



        contentLayout.addView(lectureView);


    }
    private void addQuizCard(DataSnapshot quizSnap,
                             DatabaseReference lectureRef,
                             String lectureId,
                             LinearLayout quizContainer) {


        View quizView = LayoutInflater.from(this)
                .inflate(
                        R.layout.item_quiz,
                        quizContainer,
                        false
                );


        TextView txtQuestion =
                quizView.findViewById(R.id.txtQuestion);


        ImageButton btnEditQuiz =
                quizView.findViewById(R.id.btnEditQuiz);


        ImageButton btnDeleteQuiz =
                quizView.findViewById(R.id.btnDeleteQuiz);


        LinearLayout optionsContainer =
                quizView.findViewById(R.id.optionsContainer);



        String quizId = quizSnap.getKey();


        String question =
                quizSnap.child("question")
                        .getValue(String.class);



        String correctAnswer =
                quizSnap.child("correctAnswer")
                        .getValue(String.class);



        txtQuestion.setText(
                "❓ " + question
        );



        // Edit Question

        btnEditQuiz.setOnClickListener(v -> {


            showEditDialog(
                    "Edit Question",
                    question,
                    newValue -> {


                        lectureRef.child(lectureId)
                                .child("quizzes")
                                .child(quizId)
                                .child("question")
                                .setValue(newValue);


                        loadLecturesWithQuizzes();


                    }
            );


        });



        // Delete Quiz

        btnDeleteQuiz.setOnClickListener(v -> {


            new AlertDialog.Builder(this)

                    .setTitle("Delete Quiz?")

                    .setMessage(
                            "Are you sure you want to delete this quiz?"
                    )

                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {


                                lectureRef.child(lectureId)
                                        .child("quizzes")
                                        .child(quizId)
                                        .removeValue();


                                loadLecturesWithQuizzes();


                            })


                    .setNegativeButton(
                            "Cancel",
                            null
                    )

                    .show();


        });



        // Load Options

        for(DataSnapshot optionSnap :
                quizSnap.child("options").getChildren()){


            addOptionRow(
                    optionSnap.getValue(String.class),
                    correctAnswer,
                    optionSnap.getKey(),
                    quizId,
                    lectureId,
                    lectureRef,
                    optionsContainer
            );

        }



        quizContainer.addView(quizView);


    }
    private void addOptionRow(String optionText,
                              String correctAnswer,
                              String optionIndex,
                              String quizId,
                              String lectureId,
                              DatabaseReference lectureRef,
                              LinearLayout optionsContainer) {


        View optionView = LayoutInflater.from(this)
                .inflate(
                        R.layout.item_option,
                        optionsContainer,
                        false
                );


        TextView txtOption =
                optionView.findViewById(R.id.txtOption);
        ImageButton btnEditOption =
                optionView.findViewById(R.id.btnEditOption);



        txtOption.setText(
                "• " + optionText
        );
        btnEditOption.setOnClickListener(v -> {

            showEditDialog(
                    "Edit Option",
                    optionText,
                    newValue -> {

                        lectureRef.child(lectureId)
                                .child("quizzes")
                                .child(quizId)
                                .child("options")
                                .child(optionIndex)
                                .setValue(newValue);

                        loadLecturesWithQuizzes();

                    }
            );

        });


        if(optionText != null &&
                optionText.equals(correctAnswer)) {


            txtOption.setBackgroundResource(
                    R.drawable.bg_correct_option
            );

            txtOption.setTextColor(
                    Color.parseColor("#2E7D32")
            );

            txtOption.setTypeface(
                    null,
                    Typeface.BOLD
            );


        } else {


            txtOption.setBackgroundResource(
                    R.drawable.bg_option
            );

            txtOption.setTextColor(
                    Color.parseColor("#444444")
            );

        }



        optionsContainer.addView(optionView);

    }
    private void showEditDialog(String title,
                                String oldValue,
                                OnValueSubmitted callback) {


        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);


        builder.setTitle(title);


        EditText input =
                new EditText(this);


        input.setInputType(
                InputType.TYPE_CLASS_TEXT
        );


        input.setText(oldValue);


        input.setSelection(
                input.length()
        );


        builder.setView(input);



        builder.setPositiveButton(
                "Save",
                (dialog, which) -> {


                    String value =
                            input.getText()
                                    .toString()
                                    .trim();


                    if(!value.isEmpty()) {

                        callback.onSubmit(value);

                    }

                }
        );



        builder.setNegativeButton(
                "Cancel",
                null
        );


        builder.show();

    }
}