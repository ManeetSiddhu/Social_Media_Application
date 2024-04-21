package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chitchat.Adapter.CommentAdapter;
import com.example.chitchat.Model.Comment;
import com.example.chitchat.Model.Notification;
import com.example.chitchat.Model.Post;
import com.example.chitchat.Model.User;
import com.example.chitchat.databinding.ActivityCommentBinding;
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

public class CommentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    Intent intent;
    String postId,postedBy;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Comment> list=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Toolbar
        setSupportActionBar(binding.toolbar2);
        CommentActivity.this.setTitle("Comments");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        intent =getIntent();
        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // TO FETCH DATA BY POSTEDBY AND SHOW
        database.getReference().child("posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        assert post != null;
                        // Update in new version
                        Picasso.get()
                                .load(post.getPostImage())
                                //.load(post.getPostImage())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.postImage);
                        binding.description.setText(post.getPostDescription());
                        binding.like.setText(String.valueOf(post.getPostLike()));
                        binding.comment.setText(String.valueOf(post.getCommentCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // TO FETCH DATA AND SHOW IN COMMENTACTIVITY
        database.getReference().child("Users")
                .child(postedBy).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        Picasso.get()
                                .load(user.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.profileImage);
                        binding.name.setText(user.getName());
                     }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        // ON CLICK ON COMMENT POST BTN
        binding.commentPostBtn.setOnClickListener(e->{

            //setting data by Comment model so create instance of that
            Comment comment = new Comment();
            comment.setCommentBody(binding.commentET.getText().toString());
            comment.setCommentBy(FirebaseAuth.getInstance().getUid());
            comment.setCommentAt(new Date().getTime());

            database.getReference().child("posts")
                    .child(postId).child("comments").push()
                    .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // for comment count
                            database.getReference().child("posts")
                                    .child(postId)                            // Value Event Listener is used get the previous data and then do incremnet
                                    .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int commentCount =0;
                                            if (snapshot.exists()){
                                                commentCount = snapshot.getValue(Integer.class);
                                            }
                                            database.getReference().child("posts")
                                                    .child(postId).child("commentCount")
                                                    .setValue(commentCount+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            // to blank comment section after comment
                                                            binding.commentET.setText("");
                                                            Toast.makeText(CommentActivity.this, "Commented", Toast.LENGTH_SHORT).show();

                                                            //For notification
                                                            Notification notification = new Notification();
                                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                            notification.setNotificationAt(new Date().getTime());
                                                            notification.setPostId(postId);
                                                            notification.setPostedBy(postedBy);
                                                            notification.setType("comment");

                                                            FirebaseDatabase.getInstance().getReference()
                                                                    .child("notification")
                                                                    .child(postedBy)
                                                                    .push()
                                                                    .setValue(notification);
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    });
        });


        // FOR COMMENT ADAPTER
        CommentAdapter adapter = new CommentAdapter(list,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentRv.setAdapter(adapter);
        binding.commentRv.setLayoutManager(layoutManager);

        // to get and set data
        database.getReference().child("posts").child(postId).child("comments")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            list.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}