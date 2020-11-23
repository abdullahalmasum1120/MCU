package com.aam.mcu.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aam.mcu.Profile;
import com.aam.mcu.R;
import com.aam.mcu.data.Chat;
import com.aam.mcu.data.MessageSeenPerson;
import com.aam.mcu.data.User;
import com.aam.mcu.extra.DrawerToggle;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecycler extends RecyclerView.Adapter<ChatRecycler.ViewHolder> {

    public static final int MESSAGE_TYPE_RIGHT = 1;
    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final String ACTIVE = "active";
    public static final String OFFLINE = "offline";

    Context context;
    ArrayList<Chat> chats;

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    User user;
    String seen;
    DrawerToggle toggle;

    public ChatRecycler() {
    }

    public ChatRecycler(Context context, ArrayList<Chat> chats, DrawerToggle toggle) {
        this.context = context;
        this.chats = chats;
        this.toggle = toggle;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference.child("users").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        if (viewType == MESSAGE_TYPE_RIGHT) {
            View view = layoutInflater.inflate(R.layout.message_right_view, parent, false);
            return new ViewHolder(view);
        }

        View view = layoutInflater.inflate(R.layout.message_left_view, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.showMessage.setText(chats.get(position).getMessage());

        if (!(chats.get(position).getProfileImageUrl() == null || chats.get(position).getProfileImageUrl().equals("default"))) {
            Glide.with(context).load(chats.get(position).getProfileImageUrl()).into(holder.profileImage);

            //set profile image if changed
            databaseReference.child("users").child(chats.get(position).getUid())
                    .child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.getValue().equals(chats.get(position).getProfileImageUrl())) {
                        Glide.with(context).load(snapshot.getValue()).into(holder.profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if (holder.getItemViewType() == MESSAGE_TYPE_LEFT) {
            holder.showName.setText(chats.get(position).getUsername());

            //set username if changed
//            databaseReference.child("users").child(chats.get(position).getUid())
//                    .child("username").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (!snapshot.getValue().equals(chats.get(position).getUsername())) {
//                        holder.showName.setText(snapshot.getValue().toString());
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
        }

        if (holder.getItemViewType() == MESSAGE_TYPE_LEFT){
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("uid", firebaseUser.getUid());
            hashMap.put("username", user.getUsername());
            hashMap.put("profileImageUrl",user.getProfileImageUrl());
            databaseReference.child("chats/" + chats.get(position).getMessageId() + "/seen")
                    .child(firebaseUser.getUid()).setValue(hashMap);
            if (toggle.isBadgeEnabled()) {
                toggle.setBadgeText("0");
                toggle.setBadgeEnabled(false);
            }
        }
        //active status show
        if (holder.getItemViewType() == MESSAGE_TYPE_LEFT){
            databaseReference.child("users/" + chats.get(position).getUid() + "/status")
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        if (snapshot.getValue().equals(ACTIVE)) {
                            if (!holder.active_status.isShown()) {
                                holder.active_status.setVisibility(View.VISIBLE);
                            }
                        } else if (snapshot.getValue().equals(OFFLINE)) {
                            if (holder.active_status.isShown()) {
                                holder.active_status.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        holder.showMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getItemViewType() == MESSAGE_TYPE_LEFT) {
                    if (!holder.tv_time.isShown()) {
                        holder.tv_time.setVisibility(View.VISIBLE);
                        holder.tv_time.setText(chats.get(position).getSendingTime());
                    } else if (holder.showName.isShown()) {
                        holder.tv_time.setVisibility(View.GONE);
                    }

                }

                if (holder.getItemViewType() == MESSAGE_TYPE_RIGHT) {
                    seen =  "Seen by";
                    if (!holder.showName.isShown()) {
                        holder.showName.setVisibility(View.VISIBLE);

                        databaseReference.child("chats/" + chats.get(position).getMessageId() + "/seen")
                                .addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                        MessageSeenPerson messageSeenPerson = snapshot.getValue(MessageSeenPerson.class);

                                        seen = seen + " " + messageSeenPerson.getUsername();
                                        holder.showName.setText(seen);
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    } else if (holder.showName.isShown()) {
                        holder.showName.setVisibility(View.GONE);
                    }

                }
            }
        });

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("uid", chats.get(position).getUid());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView showMessage, showName, tv_time;
        View active_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.mlv_civ_profile_image);
            showMessage = itemView.findViewById(R.id.mlv_tv_message);
            showName = itemView.findViewById(R.id.mlv_tv_username);
            tv_time = itemView.findViewById(R.id.mlv_tv_time);
            active_status = itemView.findViewById(R.id.mlv_active_status);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(chats.get(position).getUid()))
            return MESSAGE_TYPE_RIGHT;
        else
            return MESSAGE_TYPE_LEFT;
    }
}
