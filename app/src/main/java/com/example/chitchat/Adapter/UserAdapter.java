package com.example.chitchat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Model.Follow;
import com.example.chitchat.Model.Notification;
import com.example.chitchat.Model.User;
import com.example.chitchat.R;
import com.example.chitchat.databinding.UserSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder>{

    ArrayList<User> list;
    Context context;

    public UserAdapter( Context context,ArrayList<User> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        // data will come from user or by onlime
        Picasso.get()
                .load(user.getProfile())
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());

        // for checking whether that user is already followed or not
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user.getUserId())
                        .child("followers")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.follow_active_btn));
                                                    holder.binding.followBtn.setText("Following");
                                                    holder.binding.followBtn.setTextColor(context.getResources().getColor(R.color.grey));
                                                    holder.binding.followBtn.setEnabled(false);
                                                }
                                                else {
                                                    // for follower
                                                    holder.binding.followBtn.setOnClickListener(e->{

                                                        Follow follow = new Follow();
                                                        follow.setFollowedBy(FirebaseAuth.getInstance().getUid());    // by this we will get current user id
                                                        follow.setFollowedAt(new Date().getTime());                   // get current time

                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Users").child(user.getUserId())       //By user we will get position from search by position
                                                                .child("followers")
                                                                .child(FirebaseAuth.getInstance().getUid())
                                                                .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        FirebaseDatabase.getInstance().getReference()
                                                                                .child("Users").child(user.getUserId())       //By user we will get position from search by position
                                                                                .child("followerCount")
                                                                                .setValue(user.getFollowerCount()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {



                                                                                        // NOT WORKING PROPERLY

                                                                                        holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.follow_active_btn));
                                                                                        holder.binding.followBtn.setText("Following");
                                                                                        holder.binding.followBtn.setTextColor(context.getResources().getColor(R.color.grey));
                                                                                        holder.binding.followBtn.setEnabled(false);
                                                                                        Toast.makeText(context, "You followed : "+user.getName(), Toast.LENGTH_SHORT).show();

                                                                                        //FOR Notification
                                                                                        Notification notification = new Notification();
                                                                                        notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                                        notification.setNotificationAt(new Date().getTime());
                                                                                        notification.setType("follow");

                                                                                        FirebaseDatabase.getInstance().getReference().child("notification")
                                                                                                .child(user.getUserId())          //by position in that list we are accessing
                                                                                                .push()
                                                                                                .setValue(notification);
                                                                                    }
                                                                                });
                                                                    }
                                                                });

                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        UserSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserSampleBinding.bind(itemView);
        }
    }
}
