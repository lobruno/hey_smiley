package com.example.hey_smiley.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hey_smiley.MainActivity;
import com.example.hey_smiley.R;
import com.example.hey_smiley.ui.detectedFace;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {
    EditText username, email, password;
    Button btn_register;
    User user;
    FirebaseAuth auth;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.register);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (txt_username.isEmpty() || txt_email.isEmpty() || txt_password.isEmpty()){
                    Toast.makeText(RegistrationActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6 ){
                    Toast.makeText(RegistrationActivity.this, "password too short", Toast.LENGTH_SHORT).show();
                } else {
                    register(txt_username, txt_email, txt_password);

                    finish();
                }
            }
        });
    }

    private void register(final String username, String email, String password){

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();



                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", userId);
                        hashMap.put("username", username);
                        hashMap.put("img", 0.0);
                        hashMap.put("status", "offline");
                        hashMap.put("email", email.toLowerCase());

                        reference.child(userId).updateChildren(hashMap).addOnCompleteListener(
                                task2 -> {
                                    if (task2.isSuccessful())
                                        {
                                              auth.getCurrentUser().sendEmailVerification();
                                              Intent intent = new Intent(RegistrationActivity.this, detectedFace.class);
                                              startActivity(intent);
                                              finish();
                                        }
                                        else
                                           {
                                               Toast.makeText(RegistrationActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                           }});




                       } else {
                            Toast.makeText(RegistrationActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }

                    }
                );
    }


}