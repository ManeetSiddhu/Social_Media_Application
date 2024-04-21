package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.chitchat.Model.User;
import com.example.chitchat.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.signUpBtn.setOnClickListener(e->{
            String email = Objects.requireNonNull(binding.emailET.getText()).toString(),password = Objects.requireNonNull(binding.passwordET.getText()).toString();
            //String email =binding.emailET.getText().toString(),password =binding.passwordET.getText().toString();
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        User user = new User(Objects.requireNonNull(binding.nameET.getText()).toString(), Objects.requireNonNull(binding.professionalET.getText()).toString(),email,password);
                        String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        database.getReference().child("Users").child(id).setValue(user);
                        Toast.makeText(SignUpActivity.this, "User data saved", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


        binding.goToLogin.setOnClickListener(e->{
            Intent intent = new Intent(SignUpActivity.this,LogInActivity.class);
            startActivity(intent);
        });
    }
}