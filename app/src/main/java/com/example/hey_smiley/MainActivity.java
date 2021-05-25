package com.example.hey_smiley;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hey_smiley.ui.login.RegistrationActivity;
import com.example.hey_smiley.ui.login.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    AlertDialog alert;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser fire_user;
    TextView username;
    ImageView img;
    TextView mail;
    LinearLayout nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initsialization
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(MainActivity.this, anekdot(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        fab.setBackgroundColor(getResources().getColor(R.color.orange));
        NavigationView navigationView = findViewById(R.id.nav_view);

        username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
        mail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.mail);


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_chat, R.id.nav_people, R.id.nav_smileys)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //connect to firebase
        fire_user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fire_user.getUid());
        //get data from firebase
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUsername());
                mail.setText(user.getEmail());
                float smile = (user.getImg());
                //set avatar image
                if(smile > 0.8)
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.best_girl)
                            .circleCrop()
                            .into(img);
                }
                else if(smile > 0.5)
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.good_girl)
                            .circleCrop()
                            .into(img);
                }
                else if(smile > 0.25)
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.neutral_girl)
                            .circleCrop()
                            .into(img);
                }
                else
                    {
                        Glide.with(getApplicationContext())
                                .load(R.mipmap.bad_girl)
                                .circleCrop()
                                .into(img);
                    }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        isVerif();
    }

    String anekdot() throws Exception {
        String sURL = "http://rzhunemogu.ru/RandJSON.aspx?CType=1"; //just a string

        URL url = new URL(sURL);
        URLConnection request = url.openConnection();
        request.connect();
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        String anek = rootobj.getAsString();
        return  anek;
        //String zipcode = rootobj.get("zip_code").getAsString(); //just grab the zipcode
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isVerif();
        }
    };

    private void isVerif() {
        try {


            if (!(fire_user.isEmailVerified())) {
                alert = new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.alert_sms)
                        .setPositiveButton("ok", (DialogInterface.OnClickListener) listener)
                        .create();
            }
        } catch (Exception e) {
        }
    }
}