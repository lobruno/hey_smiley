package com.example.hey_smiley.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hey_smiley.R;
import com.example.hey_smiley.ui.login.User;
import com.example.hey_smiley.ui.smileys.sticker_adapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiUtils;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MessageActivity extends AppCompatActivity {

    ImageView profile_image;
    Toolbar toolbar;
    private String TYPE = "img";
    FirebaseUser fuser;
    DatabaseReference reference;
    String userid;
    String save_text = "stickers";
    RelativeLayout edit_view;
    RelativeLayout key_view;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;
    SharedPreferences spref;
    RecyclerView keyboard;
    EmojiPopup emojiPopup;
    ImageButton btn_send;
    ImageButton btn_stck;
    Button back;
    EmojiEditText text_send;
    Set<String> set;
    List<Bitmap> list;
    private List<String> str_list;
    int keyboard_numb = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new GoogleEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        set = new HashSet<>();
        keyboard = findViewById(R.id.keybaord);
        edit_view = findViewById(R.id.edit_view);
        key_view = findViewById(R.id.key_view);


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 5);
        keyboard.setLayoutManager(layoutManager);
        back = findViewById(R.id.back_emoji);


        list = new ArrayList<>();

        try {
            str_list = Load();
            if(str_list != null){

                Iterator<String> iterator = str_list.iterator();
                while(iterator.hasNext()){

                    String id = iterator.next();
                    list.add(StringToBitMap(id));
                }
            }
        } catch (Exception e){}




        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.send);
        btn_stck = findViewById(R.id.btn_stick);
        toolbar = findViewById(R.id.toolbar_sms);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        profile_image = findViewById(R.id.img_ava);
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.root_view)).build(text_send);
        text_send.disableKeyboardInput(emojiPopup);


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = text_send.getText().toString();
                if (!msg.isEmpty()){
                    sendMessage(fuser.getUid(), userid, msg, "text");
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenStick();
            }
        });

        btn_stck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               OpenStick();
            }
        });




//      add filter to edittext to write only emoji
        InputFilter filter =  new InputFilter() {
            @Override public CharSequence filter(final CharSequence source, final int start,
                                                 final int end, final Spanned dest, final int dstart,
                                                 final int dend) {
                if (!EmojiUtils.isOnlyEmojis(source.subSequence(start, end))) {
                    return "";
                }
                return null;
            }

        };

        text_send.setFilters(new InputFilter[]{filter});

        text_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                btn_stck.setVisibility(View.VISIBLE);
                return false;
            }
        });


        userid = getIntent().getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                toolbar.setTitle(user.getUsername());

                float smile = (user.getImg());
                if(smile >0.8)
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.best_girl)
                            .circleCrop()
                            .into(profile_image);

                }
                else if(smile > 0.5)
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.good_girl)
                            .circleCrop()
                            .into(profile_image);
                }
                else if(smile > 0.25)
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.neutral_girl)
                            .circleCrop()
                            .into(profile_image);
                }
                else
                {
                    Glide.with(getApplicationContext())
                            .load(R.mipmap.bad_girl)
                            .circleCrop()
                            .into(profile_image);
                }


                readMesagges(fuser.getUid(), userid, user.getImg());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!list.isEmpty())
        {
            send_sticker_adapter adapter = new send_sticker_adapter(list, fuser,userid,fuser.getUid(), userid);
            keyboard.setAdapter(adapter);
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public static void showkeyboard(Activity activity){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.showSoftInput(view, 0);
    }
    void OpenStick()
    {
        switch (keyboard_numb)
        {
            case 0:
                if(!emojiPopup.isShowing()){
                showkeyboard(MessageActivity.this);
                emojiPopup.toggle();
                edit_view.setVisibility(View.VISIBLE);
                key_view.setVisibility(View.GONE);}
                keyboard_numb++;
                break;
            case 1:
                hideKeyboard(MessageActivity.this);
                key_view.setVisibility(View.VISIBLE);
                edit_view.setVisibility(View.GONE);
                keyboard_numb--;
                break;
            case 2:
                keyboard_numb = 0;
                edit_view.setVisibility(View.VISIBLE);
                key_view.setVisibility(View.VISIBLE);

        }
    }



    List<String> Load()
    {
        spref = getSharedPreferences("pref",MODE_PRIVATE);spref = getSharedPreferences("pref", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = spref.getString(save_text, "");
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> arrayList = gson.fromJson(json, type);
        return arrayList;
    }

    private void sendMessage(String sender, final String receiver, String message, String type){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("type", type);

        reference.child("Chats").push().setValue(hashMap);


        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());

        final String msg = message;

    }


    private void readMesagges(final String myid, final String userid, final float imageurl){
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}