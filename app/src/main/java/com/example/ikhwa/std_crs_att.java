package com.example.ikhwa;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.prolificinteractive.materialcalendarview.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class std_crs_att extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView tvPresent, tvAbsent, tvLeave, tvHeader, tvSummary, tvStudentName, tvCourseName;
    private TextView tvPercentValue;
    private ProgressBar progressCircle;
    private DatabaseReference attendanceRef, studentRef, courseRef;
    private String uid, selectedDate, courseId, groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_std_crs_att);

        calendarView = findViewById(R.id.calendarView);
        tvPresent = findViewById(R.id.tvPresent);
        tvAbsent = findViewById(R.id.tvAbsent);
        tvLeave = findViewById(R.id.tvAbsent1);
        tvHeader = findViewById(R.id.tvHeader);
        tvSummary = findViewById(R.id.tvSummary);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvCourseName = findViewById(R.id.tvCourseName);
        progressCircle = findViewById(R.id.progressCircle);
        tvPercentValue = findViewById(R.id.tvPercentValue);

        calendarView.setTopbarVisible(true);
        calendarView.setTitleFormatter(day -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            return sdf.format(day.getDate());
        });
        calendarView.setHeaderTextAppearance(R.style.CalendarHeaderStyle);

        uid = SessionManager.getCurrentMemberId(this);

        if (uid == null || uid.isEmpty()) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }        courseId = getIntent().getStringExtra("courseId");
        groupId = getIntent().getStringExtra("groupId");

        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance").child(courseId).child(groupId);
        studentRef = FirebaseDatabase.getInstance().getReference("Student").child(uid);
        courseRef = FirebaseDatabase.getInstance().getReference("Courses/currentCourse").child(courseId);

        loadStudentName();
        loadCourseName();

        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        loadAttendance(selectedDate);
        decorateCalendarAndCount();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date.getDate());
            loadAttendance(selectedDate);
        });

        calendarView.setOnMonthChangedListener((widget, date) -> {
            decorateCalendarAndCount();
        });
    }

    private void loadStudentName() {
        studentRef.child("student_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) tvStudentName.setText(name);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCourseName() {
        courseRef.child("title").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) tvCourseName.setText(name);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAttendance(String selectedDate) {
        attendanceRef.child(selectedDate).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);

                if (status == null) {
                    tvSummary.setText("No attendance found for " + selectedDate);
                    return;
                }

                switch (status) {
                    case "P":
                        tvSummary.setText("You were Present on " + selectedDate);
                        break;
                    case "A":
                        tvSummary.setText("You were Absent on " + selectedDate);
                        break;
                    case "L":
                        tvSummary.setText("You were on Leave on " + selectedDate);
                        break;
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void decorateCalendarAndCount() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CalendarDay> presentDates = new ArrayList<>();
                List<CalendarDay> absentDates = new ArrayList<>();
                List<CalendarDay> leaveDates = new ArrayList<>();

                Calendar currentCal = calendarView.getCurrentDate().getCalendar();
                int currentMonth = currentCal.get(Calendar.MONTH);
                int currentYear = currentCal.get(Calendar.YEAR);

                int monthPresent = 0, monthAbsent = 0, monthLeave = 0, monthTotal = 0;

                for (DataSnapshot dateSnap : snapshot.getChildren()) {
                    String dateStr = dateSnap.getKey();
                    String status = dateSnap.child(uid).getValue(String.class);

                    if (status == null) continue;

                    try {
                        Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        CalendarDay day = CalendarDay.from(cal);

                        int snapMonth = cal.get(Calendar.MONTH);
                        int snapYear = cal.get(Calendar.YEAR);

                        if (snapMonth == currentMonth && snapYear == currentYear) {
                            switch (status) {
                                case "P":
                                    presentDates.add(day);
                                    monthPresent++;
                                    break;
                                case "A":
                                    absentDates.add(day);
                                    monthAbsent++;
                                    break;
                                case "L":
                                    leaveDates.add(day);
                                    monthLeave++;
                                    break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                monthTotal = monthPresent + monthAbsent + monthLeave;

                Drawable greenCircle = ContextCompat.getDrawable(std_crs_att.this, R.drawable.circle_green);
                Drawable redCircle = ContextCompat.getDrawable(std_crs_att.this, R.drawable.circle_red);
                Drawable yellowCircle = ContextCompat.getDrawable(std_crs_att.this, R.drawable.circle_yellow);

                calendarView.addDecorator(new CircleDecorator(greenCircle, presentDates));
                calendarView.addDecorator(new CircleDecorator(redCircle, absentDates));
                calendarView.addDecorator(new CircleDecorator(yellowCircle, leaveDates));

                tvPresent.setText("Present: " + monthPresent);
                tvAbsent.setText("Absent: " + monthAbsent);
                tvLeave.setText("Leave: " + monthLeave);

                if (monthTotal > 0) {
                    int percentage = (int) Math.round((monthPresent * 100.0) / monthTotal);
                    progressCircle.setProgress(percentage);
                    tvPercentValue.setText(percentage + "%");
                } else {
                    progressCircle.setProgress(0);
                    tvPercentValue.setText("0%");
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static class CircleDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;
        private final Drawable drawable;

        public CircleDecorator(Drawable drawable, Collection<CalendarDay> dates) {
            this.dates = new HashSet<>(dates);
            int inset = 3;
            this.drawable = new InsetDrawable(drawable, inset, inset, inset, inset);
        }

        @Override public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(drawable);
        }
    }
}
