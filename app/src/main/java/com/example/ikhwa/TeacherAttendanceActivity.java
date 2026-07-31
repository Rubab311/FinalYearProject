// File: TeacherAttendanceActivity.java
package com.example.ikhwa;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherAttendanceActivity extends AppCompatActivity {

    private LinearLayout studentAttendanceContainer;
    private DatabaseReference coursesRef, studentRef, attendanceRef;
    private String groupId, courseId;
    private String selectedDate;
    private Button saveButton, dateButton;
    private Calendar calendar;
    private final Map<String, String> attendanceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_attendance);

        studentAttendanceContainer = findViewById(R.id.studentAttendanceContainer);
        saveButton = findViewById(R.id.saveAttendanceBtn);
        dateButton = findViewById(R.id.selectDateBtn);

        groupId = getIntent().getStringExtra("groupId");
        courseId = getIntent().getStringExtra("courseId");

        coursesRef = FirebaseDatabase.getInstance().getReference("Courses/currentCourse")
                .child(courseId).child("groups").child(groupId).child("students");
        studentRef = FirebaseDatabase.getInstance().getReference("Student");
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance")
                .child(courseId).child(groupId);

        calendar = Calendar.getInstance();
        updateDate();

        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(
                    TeacherAttendanceActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        updateDate();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        saveButton.setOnClickListener(v -> saveAttendance());
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(calendar.getTime());
        dateButton.setText("Date: " + selectedDate);
        loadStudentList();
    }

    private void loadStudentList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        studentAttendanceContainer.removeAllViews();
        attendanceMap.clear();

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot studentSnap : snapshot.getChildren()) {
                    String studentUid = studentSnap.getKey();
                    if (studentUid == null) continue;

                    studentRef.child(studentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot studentData) {
                            String name = studentData.child("student_name").getValue(String.class);
                            View row = inflater.inflate(R.layout.row_attendance_item, studentAttendanceContainer, false);

                            TextView studentName = row.findViewById(R.id.studentName);
                            studentName.setText(name != null ? name : "Unnamed");

                            RadioGroup group = row.findViewById(R.id.radioGroup);
                            RadioButton radioP = row.findViewById(R.id.radio_present);
                            RadioButton radioA = row.findViewById(R.id.radio_absent);
                            RadioButton radioL = row.findViewById(R.id.radio_leave);

                            radioP.setButtonTintList(getColorStateList(R.color.blue));
                            radioA.setButtonTintList(getColorStateList(R.color.red));
                            radioL.setButtonTintList(getColorStateList(R.color.yellow));

                            // Load existing status if any
                            attendanceRef.child(selectedDate).child(studentUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String status = dataSnapshot.getValue(String.class);
                                                switch (status) {
                                                    case "P": radioP.setChecked(true); break;
                                                    case "A": radioA.setChecked(true); break;
                                                    case "L": radioL.setChecked(true); break;
                                                }
                                                attendanceMap.put(studentUid, status);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });

                            group.setOnCheckedChangeListener((g, checkedId) -> {
                                if (checkedId == R.id.radio_present) {
                                    attendanceMap.put(studentUid, "P");
                                } else if (checkedId == R.id.radio_absent) {
                                    attendanceMap.put(studentUid, "A");
                                } else if (checkedId == R.id.radio_leave) {
                                    attendanceMap.put(studentUid, "L");
                                }
                            });

                            studentAttendanceContainer.addView(row);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveAttendance() {
        if (attendanceMap.isEmpty()) {
            Toast.makeText(this, "No attendance selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        attendanceRef.child(selectedDate).setValue(attendanceMap)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Attendance saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
