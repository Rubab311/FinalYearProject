package com.example.ikhwa;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ikhwa.modules.Course;
import com.example.ikhwa.modules.Student;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Context context;
    private List<Course> courseList;

    public CourseAdapter(Context context, List<Course> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.course_card, parent, false);

        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {

        Course course = courseList.get(position);

        holder.courseTitle.setText(course.getTitle());
        holder.courseType.setText(course.getType());
        holder.duration.setText(course.getDuration());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            holder.enrollmentStatus.setText("Not Logged In");
            holder.viewButton.setEnabled(false);
            holder.viewButton.setText("Login Required");

            return;
        }

        // ==========================
        // Selected Family Member
        // ==========================

        String uid = SessionManager.getCurrentMemberId(context);



        if (uid == null || uid.isEmpty()) {
            uid = currentUser.getUid();
        }

        DatabaseReference enrollRef = FirebaseDatabase.getInstance()
                .getReference("Enrollments")
                .child(uid)
                .child(course.getId());

        enrollRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    course.setEnrolled(true);

                    holder.enrollmentStatus.setText("Enrolled");

                    holder.viewButton.setText("Enrolled");

                    holder.viewButton.setEnabled(false);

                    holder.viewButton.setBackgroundResource(R.drawable.gray_button);

                    holder.viewButton.setTextColor(Color.BLUE);

                } else {

                    course.setEnrolled(false);

                    holder.enrollmentStatus.setText("Not Enrolled");

                    holder.viewButton.setText("Enroll Now");

                    holder.viewButton.setEnabled(true);

                    holder.viewButton.setBackgroundResource(R.drawable.blue_button);

                    holder.viewButton.setTextColor(Color.WHITE);

                    holder.viewButton.setOnClickListener(v ->
                            showBottomSheet(
                                    context,
                                    course,
                                    currentUser,
                                    holder
                            ));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        context,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

            }

        });

    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView courseTitle, duration, courseType, enrollmentStatus;
        Button viewButton;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            courseTitle = itemView.findViewById(R.id.course_title);
            duration = itemView.findViewById(R.id.course_duration);
            courseType = itemView.findViewById(R.id.course_type);
            enrollmentStatus = itemView.findViewById(R.id.enroll_status);
            viewButton = itemView.findViewById(R.id.main_button);
        }
    }
    private void showBottomSheet(Context context,
                                 Course course,
                                 FirebaseUser user,
                                 CourseViewHolder holder) {

        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(context);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.course_bottom_sheet, null);

        TextView title =
                view.findViewById(R.id.bottom_sheet_course_title);

        TextView duration =
                view.findViewById(R.id.bottom_sheet_course_duration);

        TextView description =
                view.findViewById(R.id.bottom_sheet_course_description);

        Button confirmBtn =
                view.findViewById(R.id.confirm_enroll_btn);

        title.setText(course.getTitle());
        duration.setText(course.getDuration());
        description.setText(course.getDescription());

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        confirmBtn.setOnClickListener(v -> {

            // ===========================
            // Selected Family Member
            // ===========================

            String uid = SessionManager.getCurrentMemberId(context);

            if (uid == null || uid.isEmpty()) {
                uid = user.getUid();
            }

            final String memberId = uid;

            DatabaseReference studentRef =
                    FirebaseDatabase.getInstance()
                            .getReference("Student")
                            .child(memberId);

            studentRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String name = "";

                    String email = "";

                    if (snapshot.exists()) {

                        name = snapshot.child("student_name")
                                .getValue(String.class);

                        email = snapshot.child("email")
                                .getValue(String.class);
                    }

                    if (name == null) name = "";
                    if (email == null) email = "";

                    Student student =
                            new Student(memberId, name, email);

                    DatabaseReference enrollRef =
                            FirebaseDatabase.getInstance()
                                    .getReference("Enrollments")
                                    .child(memberId)
                                    .child(course.getId());

                    enrollRef.addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(
                                        @NonNull DataSnapshot enrollSnap) {

                                    if (enrollSnap.exists()) {

                                        Toast.makeText(
                                                context,
                                                "Already Enrolled",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    } else {

                                        enrollCourse(
                                                context,
                                                course,
                                                student
                                        );

                                        course.setEnrolled(true);

                                        notifyItemChanged(
                                                holder.getAdapterPosition()
                                        );

                                        if (context instanceof StudentHome2) {
                                            ((StudentHome2) context).recreate();
                                        }

                                        Toast.makeText(
                                                context,
                                                "Enrollment Successful",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    bottomSheetDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(
                                        @NonNull DatabaseError error) {

                                    Toast.makeText(
                                            context,
                                            error.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();

                                }

                            });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(
                            context,
                            error.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                }

            });

        });

    }
    private void enrollCourse(Context context, Course course, Student student) {

        DatabaseReference courseRef = FirebaseDatabase.getInstance()
                .getReference("Courses")
                .child("currentCourse")
                .child(course.getId());

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot courseSnap) {

                if (!courseSnap.exists()) {
                    return;
                }

                String courseId = course.getId();

                // ===============================
                // Save Enrollment
                // ===============================

                DatabaseReference enrollmentRef =
                        FirebaseDatabase.getInstance()
                                .getReference("Enrollments")
                                .child(student.getStudentId())
                                .child(courseId);

                enrollmentRef.setValue(true);

                // ===============================
                // Increase Student Count
                // ===============================

                DatabaseReference countRef =
                        courseRef.child("enrolledStudents");

                countRef.runTransaction(new Transaction.Handler() {

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(
                            @NonNull MutableData currentData) {

                        Long value = currentData.getValue(Long.class);

                        if (value == null) {
                            currentData.setValue(1);
                        } else {
                            currentData.setValue(value + 1);
                        }

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error,
                                           boolean committed,
                                           DataSnapshot currentData) {

                        if (!committed)
                            return;

                        String type =
                                courseSnap.child("type")
                                        .getValue(String.class);

                        if (type != null &&
                                type.equalsIgnoreCase("Attendance Based")) {

                            DatabaseReference groupsRef =
                                    courseRef.child("groups");

                            groupsRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {

                                        @Override
                                        public void onDataChange(
                                                @NonNull DataSnapshot snapshot) {

                                            boolean assigned = false;

                                            for (DataSnapshot group :
                                                    snapshot.getChildren()) {

                                                long size = group.child("students")
                                                        .getChildrenCount();

                                                if (size < 2) {

                                                    assignToGroup(
                                                            group.getRef(),
                                                            student
                                                    );

                                                    assigned = true;
                                                    break;
                                                }
                                            }

                                            if (!assigned) {

                                                String newGroup =
                                                        "group" +
                                                                (snapshot.getChildrenCount() + 1);

                                                assignToGroup(
                                                        groupsRef.child(newGroup),
                                                        student
                                                );

                                            }

                                        }

                                        @Override
                                        public void onCancelled(
                                                @NonNull DatabaseError error) {

                                        }

                                    });

                        }

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    private void assignToGroup(DatabaseReference groupRef,
                               Student student) {

        HashMap<String, Object> map = new HashMap<>();

        map.put("uid", student.getStudentId());
        map.put("name", student.getStudent_name());
        map.put("email", student.getEmail());

        groupRef.child("students")
                .child(student.getStudentId())
                .setValue(map);

    }

}