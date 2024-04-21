package com.example.chitchat.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chitchat.Adapter.FollowersAdapter;
import com.example.chitchat.Model.Follow;
import com.example.chitchat.Model.User;
import com.example.chitchat.R;
import com.example.chitchat.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;


public class ProfileFragment extends Fragment {

    ArrayList<Follow> list;
    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // to fetch data from database
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);               //if snapshot means user having any value
                    //  When we get image online from database then it will be in form of piccasso
                    assert user != null;
                    Picasso.get()
                            .load(user.getCoverPhoto())
                            .placeholder(R.drawable.placeholder)
                            .into(binding.coverPhoto);
                    // for profile
                    Picasso.get()
                            .load(user.getProfile())
                                    .placeholder(R.drawable.placeholder)
                                            .into(binding.profileImage);

                    binding.userName.setText(user.getName());
                    binding.profession.setText(user.getProfession());
                    binding.followers.setText(String.valueOf(user.getFollowerCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        list = new ArrayList<>();
        /*list.add(new Follow(R.drawable.story_pic));
        list.add(new Follow(R.drawable.story_pic2));
        list.add(new Follow(R.drawable.story_pic));
        list.add(new Follow(R.drawable.story_pic2));*/
        FollowersAdapter adapter = new FollowersAdapter(list,getContext());
        LinearLayoutManager layoutManager =new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.friendRV.setAdapter(adapter);
        binding.friendRV.setLayoutManager(layoutManager);

        // for adding followers of user in profile in MyFriends or My Followers recycler view
        database.getReference().child("Users")
                        .child(auth.getUid())                  // for fetching user who login
                .child("followers").addValueEventListener(new ValueEventListener() {    // value event listener is addedto getr all data inside followers
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                            // Follow model is taken because data inside the followers have this type
                            Follow follow = dataSnapshot.getValue(Follow.class);
                            list.add(follow);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.changeCoverPhoto.setOnClickListener(e->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);   // which type of action to be performed
            intent.setType("image/*");                     // which type of content to be get
            startActivityForResult(intent,11);
        });
        // for profile we have used verified icon but it will be done via activity
        binding.verifiedAccount.setOnClickListener(e->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);   // which type of action to be performed
            intent.setType("image/*");                     // which type of content to be get
            startActivityForResult(intent,22);
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            if (data!=null){
                Uri uri = data.getData();
                binding.coverPhoto.setImageURI(uri);      // for update and show in cover photo

                // for store in firebase
                final StorageReference reference = storage.getReference().child("cover_photo")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                assert uri != null;
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // For data to be store in database from where we can fetch
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(getContext(), "cover_photo saved", Toast.LENGTH_SHORT).show();
                                database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("coverPhoto").setValue(uri.toString());
                            }
                        });
                    }
                });
            }
        }
        else {
            if (data.getData()!=null){
                Uri uri = data.getData();
                binding.profileImage.setImageURI(uri);

                final StorageReference reference = storage.getReference().child("profile_image").child(FirebaseAuth.getInstance().getUid());
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(getContext(), "profile_photo saved", Toast.LENGTH_SHORT).show();
                                database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("profile").setValue(uri.toString());

                            }
                        });
                    }
                });
            }
        }

    }
}