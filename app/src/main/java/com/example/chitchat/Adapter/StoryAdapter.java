package com.example.chitchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Model.Story;
import com.example.chitchat.Model.User;
import com.example.chitchat.Model.UserStories;
import com.example.chitchat.R;
import com.example.chitchat.databinding.StoryRvDesignBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder> {

    ArrayList<Story> list;
    Context context;

    public StoryAdapter(ArrayList<Story> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_rv_design,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Story story = list.get(position);

        // To get last story
        if (story.getStories().size()>0) {
            UserStories lastStory = story.getStories().get(story.getStories().size() - 1);
            Picasso.get()
                    .load(lastStory.getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.binding.storyImage);
            // for status circle
            holder.binding.statusCircle.setPortionsCount(story.getStories().size());

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(story.getStoryBy()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            assert user != null;
                            Picasso.get().load(user.getProfile())
                                    .placeholder(R.drawable.placeholder)
                                    .into(holder.binding.profileImage);
                            holder.binding.name.setText(user.getName());

                            // to see story while click on Story
                            holder.binding.storyImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    ArrayList<MyStory> myStories = new ArrayList<>();

                                    for (UserStories stories : story.getStories()) {
                                        myStories.add(new MyStory(
                                                stories.getImage()
                                        ));
                                    }
                                    new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                                            .setStoriesList(myStories) // Required
                                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                            .setTitleText(user.getName()) // Default is Hidden
                                            .setSubtitleText("") // Default is Hidden
                                            .setTitleLogoUrl(user.getProfile()) // Default is Hidden
                                            .setStoryClickListeners(new StoryClickListeners() {
                                                @Override
                                                public void onDescriptionClickListener(int position) {
                                                    //your action
                                                }

                                                @Override
                                                public void onTitleIconClickListener(int position) {
                                                    //your action
                                                }
                                            }) // Optional Listeners
                                            .build() // Must be called before calling show method
                                            .show();

                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        StoryRvDesignBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = StoryRvDesignBinding.bind(itemView);
        }
    }
}
