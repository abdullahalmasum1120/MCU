package com.aam.mcu.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.Profile;
import com.aam.mcu.R;
import com.aam.mcu.data.Comment;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CommentRecycler extends RecyclerView.Adapter<CommentRecycler.ViewHolder> {

    private Context context;
    private ArrayList<Comment> comments;
    private DatabaseReference databaseReference;

    public CommentRecycler(Context context, ArrayList<Comment> comments) {
        this.context = context;
        this.comments = comments;

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_comment_view, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (comments.size() != 0){

            Glide.with(context).load(comments.get(position).getProfileImageUrl()).into(holder.iv_profileImage);
            holder.tv_username.setText(comments.get(position).getUsername());
            holder.tv_time.setText(comments.get(position).getCommentTime());
            holder.tv_comment.setText(comments.get(position).getComment());

            //set profile image if changed
            databaseReference.child("users").child(comments.get(position).getUid())
                    .child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!Objects.equals(snapshot.getValue(), comments.get(position).getProfileImageUrl())){
                        Glide.with(context).load(snapshot.getValue()).into(holder.iv_profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //set username if changed
            databaseReference.child("users").child(comments.get(position).getUid())
                    .child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!Objects.equals(snapshot.getValue(), comments.get(position).getUsername())){
                        holder.tv_username.setText(snapshot.getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //handling click event
            holder.iv_profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Profile.class);
                    intent.putExtra("uid", comments.get(position).getUid());
                    context.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_profileImage;
        TextView tv_username, tv_time, tv_comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_profileImage = itemView.findViewById(R.id.scv_civ_profile_image);
            tv_username = itemView.findViewById(R.id.scv_tv_username);
            tv_time = itemView.findViewById(R.id.scv_tv_time);
            tv_comment = itemView.findViewById(R.id.scv_tv_comment);
        }
    }
}
