package com.example.chitchat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.CommentActivity;
import com.example.chitchat.Model.Notification;
import com.example.chitchat.Model.Post;
import com.example.chitchat.Model.User;
import com.example.chitchat.R;
import com.example.chitchat.databinding.DashboardRvSampleBinding;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    ArrayList<Post> list;
    Context context;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @SuppressLint("SetTextI18n") // In updated version
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

       Post model = list.get(position);

       // FOR UPLOADING POST

       Picasso.get()
                .load(model.getPostImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.postImage);
        holder.binding.like.setText(model.getPostLike()+"");
        holder.binding.comment.setText(model.getCommentCount()+"");
        String description = model.getPostDescription();
        if (description.equals("")){
            holder.binding.postDescription.setVisibility(View.GONE);
        }else {
            holder.binding.postDescription.setText(model.getPostDescription());

        }
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(model.getPostedBy()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);                                 // All  data stored in user
                        assert user != null;
                        Picasso.get()
                                .load(user.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(holder.binding.profileImage);
                        holder.binding.userName.setText(user.getName());
                        holder.binding.bio.setText(user.getProfession());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference()
                .child("posts").child(model.getPostId())                           // Post Id is due to 84 line in Home Fragment
                .child("likes").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart,0,0,0);
                                }
                                else {
                                    // FOR LIKE
                                    holder.binding.like.setOnClickListener(e->{
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("posts").child(model.getPostId())                           // Post Id is due to 84 line in Home Fragment
                                                .child("likes").child(FirebaseAuth.getInstance().getUid())
                                                .setValue(true)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        // for increment in postLike
                                                        FirebaseDatabase.getInstance().getReference().child("posts")
                                                                .child(model.getPostId()).child("postLike")
                                                                .setValue(model.getPostLike()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart,0,0,0);

                                                                        // For notification
                                                                        Notification notification = new Notification();
                                                                        notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                        notification.setNotificationAt(new Date().getTime());
                                                                        notification.setPostId(model.getPostId());            //By model of Post we get an postId
                                                                        notification.setPostedBy(model.getPostedBy());
                                                                        notification.setType("like");

                                                                        FirebaseDatabase.getInstance().getReference().child("notification")
                                                                                .child(model.getPostedBy())
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
        holder.binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId",model.getPostId());
                intent.putExtra("postedBy",model.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        DashboardRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = DashboardRvSampleBinding.bind(itemView);
        }
    }
}
