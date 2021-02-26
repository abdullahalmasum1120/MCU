package com.aam.mcu.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.Comments;
import com.aam.mcu.Profile;
import com.aam.mcu.R;
import com.aam.mcu.data.Post;
import com.aam.mcu.dialogues.EditPost;
import com.aam.mcu.dialogues.ProgressBar;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeRecycler extends RecyclerView.Adapter<HomeRecycler.ViewHolder> {

    private final int ONLY_IMAGE = 11;
    private final int ONLY_TEXT = 12;
    private final int FULL_POST = 13;
    private ArrayList<Post> posts;
    private Context context;
    ProgressBar progressBar;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    public HomeRecycler() {
    }

    public HomeRecycler(Context context, final ArrayList<Post> posts, ProgressBar progressBar) {
        this.context = context;
        this.posts = posts;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.progressBar = progressBar;
    }

    @NonNull
    @Override
    public HomeRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == FULL_POST){
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.single_post_view_main, parent, false);
            return new ViewHolder(view);
        } else if (viewType == ONLY_IMAGE){
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.single_post_view_image, parent, false);
            return new ViewHolder(view);
        } else if (viewType == ONLY_TEXT){
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.single_post_view_text, parent, false);
            return new ViewHolder(view);
        }
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.single_post_view_text, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeRecycler.ViewHolder holder, final int position) {
        //dismiss progressbar
        progressBar.dismiss();

        //set profile image
        if (posts.get(position).getUid() != null) {
            databaseReference.child("users/" + posts.get(position).getUid() + "/profileImageUrl")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Glide.with(context).load(snapshot.getValue()).into(holder.iv_profile_image);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        //set post owner name
        if (posts.get(position).getUid() != null) {
            databaseReference.child("users/" + posts.get(position).getUid() + "/username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            holder.tv_name.setText(snapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        //set time of the post has been created
        if (posts.get(position).getPostTime() != null) {
            holder.tv_time.setText(posts.get(position).getPostTime());
        }
        //set if post has an image
        if (holder.getItemViewType() == ONLY_IMAGE || holder.getItemViewType() == FULL_POST){
            if (posts.get(position).getPostImageUrl() != null) {
                if (posts.get(position).getPostImageUrl().equals("default")) {
                    databaseReference.child("posts/" + posts.get(position).getPostId() + "/postImageUrl")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        if (!snapshot.getValue().equals("default")) {
                                            if (!holder.iv_post_image.isShown()) {
                                                holder.iv_post_image.setVisibility(View.VISIBLE);
                                            }
                                            Glide.with(context).load(posts.get(position).getPostImageUrl()).into(holder.iv_post_image);
                                        } else {
                                            if (holder.iv_post_image.isShown()) {
                                                holder.iv_post_image.setVisibility(View.GONE);
                                            }
                                        }
                                    } else {
                                        if (holder.iv_post_image.isShown()) {
                                            holder.iv_post_image.setVisibility(View.GONE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                } else {
                    if (!holder.iv_post_image.isShown()) {
                        holder.iv_post_image.setVisibility(View.VISIBLE);
                    }
                    Glide.with(context).load(posts.get(position).getPostImageUrl()).into(holder.iv_post_image);
                }
            } else {
                if (holder.iv_post_image.isShown()) {
                    holder.iv_post_image.setVisibility(View.GONE);
                }
            }
        }

        //set post body
        if (holder.getItemViewType() == ONLY_TEXT || holder.getItemViewType() == FULL_POST){
            if (posts.get(position).getPostBody() != null) {
                databaseReference.child("posts/" + posts.get(position).getPostId() + "/postBody")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    if (!holder.tv_post_body.isShown()) {
                                        holder.tv_post_body.setVisibility(View.VISIBLE);
                                    }
                                    holder.tv_post_body.setText(snapshot.getValue(String.class));
                                } else {
                                    if (holder.tv_post_body.isShown()) {
                                        holder.tv_post_body.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                if (holder.tv_post_body.isShown()) {
                    holder.tv_post_body.setVisibility(View.GONE);
                }
            }
        }

        //set total like
        databaseReference.child("posts/" + posts.get(position).getPostId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            if (snapshot.hasChild("likes")) {
                                if (!holder.tv_like.isShown()){
                                    holder.tv_like.setVisibility(View.VISIBLE);
                                }
                                holder.tv_like.setText(String.valueOf(snapshot.child("likes").getChildrenCount()));

                                if (snapshot.child("likes").hasChild(firebaseUser.getUid())) {
                                    holder.iv_like.setImageTintList(ColorStateList.valueOf(Color.BLUE));
                                } else {
                                    holder.iv_like.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                                }
                            } else {
                                if (holder.tv_like.isShown()){
                                    holder.tv_like.setVisibility(View.GONE);
                                }
                                holder.iv_like.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //set total comment
        databaseReference.child("posts/" + posts.get(position).getPostId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            if (snapshot.hasChild("comments")) {
                                if (!holder.tv_comment.isShown()){
                                    holder.tv_comment.setVisibility(View.VISIBLE);
                                }
                                holder.tv_comment.setText(String.valueOf(snapshot.child("comments").getChildrenCount()));
                            } else {
                                if (holder.tv_comment.isShown()){
                                    holder.tv_comment.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //set total seen
        databaseReference.child("posts/" + posts.get(position).getPostId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            if (snapshot.hasChild("seen")) {
                                if (!holder.tv_seen.isShown()){
                                    holder.tv_seen.setVisibility(View.VISIBLE);
                                }
                                holder.tv_seen.setText(String.valueOf(snapshot.child("seen").getChildrenCount()));
                            } else {
                                if (holder.tv_seen.isShown()){
                                    holder.tv_seen.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ////////
        //profile onClick events
        holder.iv_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("uid", posts.get(position).getUid());
                context.startActivity(intent);
            }
        });

        //image onclick event
        if (holder.getItemViewType() == FULL_POST || holder.getItemViewType() == ONLY_IMAGE){
            holder.iv_post_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //animation
                    holder.iv_post_image.startAnimation(AnimationUtils.loadAnimation(context, R.anim.zoom_out));

                    showFullImage(Uri.parse(posts.get(position).getPostImageUrl()));

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("uid", firebaseUser.getUid());
                    hashMap.put("time", Calendar.getInstance().getTime().toString());

                    databaseReference.child("posts/" + posts.get(position).getPostId() + "/seen")
                            .child(firebaseUser.getUid()).setValue(hashMap);
                }
            });
        }

        //like onClick event
        holder.iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animation
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                animation.setDuration(1000);
                holder.iv_like.startAnimation(animation);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("uid", firebaseUser.getUid());
                hashMap.put("time", Calendar.getInstance().getTime().toString());

                databaseReference.child("posts/" + posts.get(position).getPostId() + "/likes")
                        .child(firebaseUser.getUid()).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "liked", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //seen onClick event
        holder.seen_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //comment onClick event
        holder.comment_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Comments.class);
                intent.putExtra("postId", posts.get(position).getPostId());
                context.startActivity(intent);
            }
        });

        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.edit) {
                            if (posts.get(position).getUid().equals(firebaseUser.getUid())) {
                                EditPost editPost = new EditPost(context, posts.get(position));
                                editPost.show();
                            } else {
                                Toast.makeText(context, "You can not edit others post", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (item.getItemId() == R.id.delete) {
                            if (posts.get(position).getUid().equals(firebaseUser.getUid())) {
                                databaseReference.child("posts").child(posts.get(position).getPostId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "You can not delete others post", Toast.LENGTH_SHORT).show();
                            }
                        }
                        return true;
                    }
                });
                popupMenu.inflate(R.menu.single_post);
                popupMenu.show();
            }
        });
    }

    private void showFullImage(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView iv_profile_image;
        ImageView iv_post_image, iv_seen, iv_like, iv_more;
        TextView tv_name;
        TextView tv_post_body;
        TextView tv_seen, tv_like;
        TextView tv_comment;
        TextView tv_time;
        RelativeLayout comment_parent, seen_parent, like_parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_profile_image = itemView.findViewById(R.id.spv_civ_profile_image);
            iv_post_image = itemView.findViewById(R.id.spv_iv_post_image);

            tv_name = itemView.findViewById(R.id.spv_tv_username);
            tv_post_body = itemView.findViewById(R.id.spv_tv_post_body);
            tv_seen = itemView.findViewById(R.id.spv_tv_seen);
            tv_comment = itemView.findViewById(R.id.spv_tv_comment);
            tv_time = itemView.findViewById(R.id.spv_tv_time);
            tv_like = itemView.findViewById(R.id.spv_tv_like);

            comment_parent = itemView.findViewById(R.id.spv_comment_parent);
            seen_parent = itemView.findViewById(R.id.spv_seen_parent);
            like_parent = itemView.findViewById(R.id.spv_like_parent);

            iv_seen = itemView.findViewById(R.id.spv_iv_seen);
            iv_like = itemView.findViewById(R.id.spv_iv_like);
            iv_more = itemView.findViewById(R.id.spv_iv_more_options);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (posts.get(position).getPostBody() != null && posts.get(position).getPostImageUrl() != null) {
            return FULL_POST;
        }
        else if (posts.get(position).getPostImageUrl() != null && posts.get(position).getPostBody() == null) {
            return ONLY_IMAGE;
        }
        else if (posts.get(position).getPostImageUrl() == null && posts.get(position).getPostBody() != null) {
            return ONLY_TEXT;
        }
        return FULL_POST;
    }
}
