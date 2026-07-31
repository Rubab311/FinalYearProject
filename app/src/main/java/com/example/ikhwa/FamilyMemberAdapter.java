package com.example.ikhwa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.MemberViewHolder> {

    private ArrayList<MemberModel> memberList;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onSwitch(MemberModel member);
        void onDelete(MemberModel member);
    }

    public FamilyMemberAdapter(ArrayList<MemberModel> memberList,
                               OnMemberActionListener listener) {
        this.memberList = memberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.family_member_item, parent, false);

        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {

        MemberModel member = memberList.get(position);

        holder.tvMemberName.setText(member.getName());
        holder.tvRelation.setText(member.getRelation());

        holder.imgSwitch.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSwitch(member);
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        TextView tvMemberName, tvRelation;
        ImageView imgSwitch, imgDelete;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvRelation = itemView.findViewById(R.id.tvRelation);

            imgSwitch = itemView.findViewById(R.id.imgSwitch);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}