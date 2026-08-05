package com.example.ikhwa;

import static androidx.core.content.ContextCompat.startActivity;
import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ikhwa.modules.Course;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CourseActivity extends AppCompatActivity {

    private LinearLayout currentCourseContainer, previousCourseContainer;
    private DatabaseReference courseRef;
    private FloatingActionButton btnAddCourse;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        courseRef = FirebaseDatabase.getInstance().getReference("Courses");

        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, AdminHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        currentCourseContainer = findViewById(R.id.current_course_container);
        previousCourseContainer = findViewById(R.id.previous_course_container);
        btnAddCourse = findViewById(R.id.btn_add_course);

        btnAddCourse.setOnClickListener(v -> showAddCourseDialog());
        loadCoursesFromFirebase();
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.activity_course_form_dialog, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.et_name);
        EditText etDesc = view.findViewById(R.id.et_id);
        EditText etDuration = view.findViewById(R.id.et_job_title);
        EditText etStart = view.findViewById(R.id.et_join_date);
        EditText etEnd = view.findViewById(R.id.et_end_date);
        RadioGroup rgCourseType = view.findViewById(R.id.rg_course_type);
        RadioButton rbAttendance = view.findViewById(R.id.rb_attendance);
        RadioButton rbQuiz = view.findViewById(R.id.rb_quiz);
        TextView btnSave = view.findViewById(R.id.btn_save);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);

        etStart.setOnClickListener(v -> showDatePickerDialog(etStart));
        etEnd.setOnClickListener(v -> showDatePickerDialog(etEnd));

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String title = etName.getText().toString();
            String description = etDesc.getText().toString();
            String duration = etDuration.getText().toString();
            String startDate = etStart.getText().toString();
            String endDate = etEnd.getText().toString();

            int selectedId = rgCourseType.getCheckedRadioButtonId();
            String type = "";
            if (selectedId == R.id.rb_attendance) {
                type = "Attendance Based";
            } else if (selectedId == R.id.rb_quiz) {
                type = "Quiz Based";
            }

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(type)) {
                return;
            }

            String id = courseRef.push().getKey();
            Course model = new Course(title, description, duration, startDate, endDate, type);
            model.setId(id);
            model.setType(type);

            boolean isCurrent = isCurrentCourse(endDate);
            String category = isCurrent ? "currentCourse" : "previousCourse";

            if (id != null) {
                courseRef.child(category).child(id).setValue(model);
            }

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CourseActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    editText.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadCoursesFromFirebase() {
        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentCourseContainer.removeAllViews();
                previousCourseContainer.removeAllViews();

                for (DataSnapshot data : snapshot.child("currentCourse").getChildren()) {
                    Course model = data.getValue(Course.class);
                    if (model != null) {
                        model.setId(data.getKey());
                        addCourseToLayout(model, true);
                    }
                }

                for (DataSnapshot data : snapshot.child("previousCourse").getChildren()) {
                    Course model = data.getValue(Course.class);
                    if (model != null) {
                        model.setId(data.getKey());
                        addCourseToLayout(model, false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean isCurrentCourse(String endDate) {
        try {
            Date end = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDate);

            // Compare with today's date (without time part)
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            today = sdf.parse(sdf.format(today));

            return !today.after(end); // true if today <= endDate
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void addCourseToLayout(Course model, boolean isCurrent) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400, 300);
        params.setMargins(16, 16, 16, 16);
        frame.setLayoutParams(params);
        frame.setBackgroundResource(R.drawable.std_shape1);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setGravity(Gravity.CENTER);
        innerLayout.setPadding(6, 6, 6, 6);

        TextView titleText = new TextView(this);
        titleText.setText(model.getTitle());
        titleText.setTextColor(Color.parseColor("#3F51B5"));
        titleText.setTextSize(16);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleText.setMaxLines(2);
        titleText.setEllipsize(TextUtils.TruncateAt.END);
        titleText.setPadding(5,5,5,5);

        TextView typeText = new TextView(this);
        typeText.setText(model.getType());
        typeText.setTextColor(Color.DKGRAY);
        typeText.setTextSize(12);
        typeText.setGravity(Gravity.CENTER);
        typeText.setPadding(5,5,5,5);

        innerLayout.addView(titleText);
        innerLayout.addView(typeText);

        frame.addView(innerLayout);

        // ACTION on click
        frame.setOnClickListener(v -> {
            showCourseDetailsDialog(model);  // Always show dialog
        });

        if (isCurrent)
            currentCourseContainer.addView(frame);
        else
            previousCourseContainer.addView(frame);
    }

    private void showCourseDetailsDialog(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.course_details_dialog, null);
        builder.setView(view);

        TextView titleText = view.findViewById(R.id.course_title);
        TextView descriptionText = view.findViewById(R.id.course_description);
        TextView durationText = view.findViewById(R.id.duration_value);
        TextView studentCountText = view.findViewById(R.id.students_value);
        TextView testInfoText = view.findViewById(R.id.test_value);

        Button btnViewGroups = view.findViewById(R.id.crs_reg);
        Button btnDelete = view.findViewById(R.id.crs_del);
        Button btnProgress = view.findViewById(R.id.btn_progress);
        ImageView closeBtn = view.findViewById(R.id.close_btn);

        // Change button text based on course type
        if ("Quiz Based".equalsIgnoreCase(course.getType())) {
            btnViewGroups.setText("View Lecture");
        } else {
            btnViewGroups.setText("View Groups");
        }

        if (titleText != null) titleText.setText(course.getTitle());
        if (descriptionText != null) descriptionText.setText(course.getDescription());
        if (durationText != null) durationText.setText(course.getDuration());
        if (testInfoText != null) testInfoText.setText(course.getType());

        if (studentCountText != null) {
            DatabaseReference enrolledRef = FirebaseDatabase.getInstance()
                    .getReference("Courses")
                    .child("currentCourse")
                    .child(course.getId())
                    .child("enrolledStudents");

            enrolledRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long count = snapshot.getValue(Long.class);
                    studentCountText.setText(count != null ? String.valueOf(count) : "0");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    studentCountText.setText("N/A");
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        btnDelete.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, DeleteCourseActivity.class);
            intent.putExtra("courseId", course.getId());
            startActivity(intent);
            dialog.dismiss();
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        btnProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CourseActivity.this,
                    AdminCourseProgressActivity.class
            );

            intent.putExtra("courseId", course.getId());
            intent.putExtra("courseTitle", course.getTitle());

            // ADD THIS LINE
            intent.putExtra("courseType", course.getType());

            startActivity(intent);

            dialog.dismiss();
        });
        btnViewGroups.setOnClickListener(v -> {
            if ("Quiz Based".equalsIgnoreCase(course.getType())) {
                Intent intent = new Intent(CourseActivity.this, CourseContentActivity.class);
                intent.putExtra("courseId", course.getId());
                startActivity(intent);
                dialog.dismiss();
            } else {
                Intent intent = new Intent(CourseActivity.this, CreateGroupForCourseActivity.class);
                intent.putExtra("courseId", course.getId());
                intent.putExtra("courseTitle", course.getTitle());
                startActivity(intent);
                dialog.dismiss();
            }
        });
    }
}
