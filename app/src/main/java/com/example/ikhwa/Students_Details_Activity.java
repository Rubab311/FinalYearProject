package com.example.ikhwa;

import
        android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.example.ikhwa.modules.StudentDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.ValueEventListener;

public class Students_Details_Activity extends AppCompatActivity {

    LinearLayout studentContainer;
Button backbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_details);
backbtn=findViewById(R.id.back_btn_stu);
        studentContainer = findViewById(R.id.students_container);
backbtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(Students_Details_Activity.this, AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }
});
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Student");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentContainer.removeAllViews(); // clear before adding

                for (DataSnapshot snap : snapshot.getChildren()) {
                    StudentDetails student = snap.getValue(StudentDetails.class);

                    if (student != null) {
                        View cardView = LayoutInflater.from(Students_Details_Activity.this)
                                .inflate(R.layout.student_details_item, studentContainer, false);

                        TextView nameTV = cardView.findViewById(R.id.student_name);
                        nameTV.setText(student.getStudent_name());

                        Button btnSeeMore = cardView.findViewById(R.id.btn_see_details_student);
                        btnSeeMore.setOnClickListener(v -> showDetailsDialog(student));

                        studentContainer.addView(cardView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Students_Details_Activity.this, "Failed to load TeacherRequests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDetailsDialog(StudentDetails staff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_details, null);

        // Find and set data in views
        TextView tvName = dialogView.findViewById(R.id.dialog_student_name);
        TextView tvFatherName = dialogView.findViewById(R.id.dialog_student_fathername);
        TextView tvEmail = dialogView.findViewById(R.id.dialog_student_email);
        Button btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        tvName.setText("Name: " + staff.getStudent_name());
        tvFatherName.setText("Father Name: " + staff.getFather_name());
        tvEmail.setText("Email: " + staff.getEmail());
        TextView tvAge = dialogView.findViewById(R.id.dialog_student_age);
        TextView tvPhone = dialogView.findViewById(R.id.dialog_student_phone);
        TextView tvAddress = dialogView.findViewById(R.id.dialog_student_address);

        tvAge.setText("Age: " + staff.getAge());
        tvPhone.setText("Phone: " + staff.getPhone());
        tvAddress.setText("Address: " + staff.getAddress());
        builder.setView(dialogView); // Set custom view

        // Create dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Close button action
        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

}

