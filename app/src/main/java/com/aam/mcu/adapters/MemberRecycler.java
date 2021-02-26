package com.aam.mcu.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.Profile;
import com.aam.mcu.R;
import com.aam.mcu.data.User;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberRecycler extends RecyclerView.Adapter<MemberRecycler.ViewHolder> {

    private static final String ACTIVE = "active";
    private static final String OFFLINE = "offline";

    private ArrayList<User> users;
    private Context context;
    private DatabaseReference databaseReference;

    public MemberRecycler() {
    }

    public MemberRecycler(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.single_member_view, parent, false);

        return new MemberRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        if (!(users.get(position).getProfileImageUrl() == null || users.get(position).getProfileImageUrl().equals("default"))) {
            Glide.with(context).load(users.get(position).getProfileImageUrl()).into(holder.ivProfileImg);

            //set profile image if changed
            databaseReference.child("users").child(users.get(position).getUserId())
                    .child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!Objects.equals(snapshot.getValue(), users.get(position).getProfileImageUrl())) {
                        Glide.with(context).load(snapshot.getValue()).into(holder.ivProfileImg);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        if (users.get(position).getUsername() != null) {
            holder.tvProfileName.setText(users.get(position).getUsername());

            //set username if changed
            databaseReference.child("users").child(users.get(position).getUserId())
                    .child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!Objects.equals(snapshot.getValue(), users.get(position).getUsername())) {
                        holder.tvProfileName.setText(snapshot.getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        holder.ivProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("uid", users.get(position).getUserId());
                context.startActivity(intent);
            }
        });

        if (users.get(position).getStatus() != null) {
            databaseReference.child("users").child(users.get(position).getUserId()).child("status")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                if (snapshot.getValue().equals(ACTIVE)) {
                                    if (!holder.status.isShown()) {
                                        holder.status.setVisibility(View.VISIBLE);
                                    }

                                    holder.active_status.setText(ACTIVE);
                                } else if (snapshot.getValue().equals(OFFLINE)) {
                                    if (holder.status.isShown()) {
                                        holder.status.setVisibility(View.GONE);
                                    }

                                    holder.active_status.setText(OFFLINE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            holder.active_status.setText(OFFLINE);
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfileImg;
        TextView tvProfileName, active_status;
        View status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImg = itemView.findViewById(R.id.smv_civ_profile_image);
            tvProfileName = itemView.findViewById(R.id.smv_tv_username);
            active_status = itemView.findViewById(R.id.smv_tv_status);
            status = itemView.findViewById(R.id.smv_active_status);
        }
    }
}
