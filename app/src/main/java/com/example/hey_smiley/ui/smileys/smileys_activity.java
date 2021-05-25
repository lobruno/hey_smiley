package com.example.hey_smiley.ui.smileys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.ArraySet;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hey_smiley.MainActivity;
import com.example.hey_smiley.R;
import com.example.hey_smiley.ui.sticker_adapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class smileys_activity extends AppCompatActivity {
    private static final int REQUEST_TAKE_PHOTO = 2;
    List<Bitmap> list;
    ArrayList<String> str_list = new ArrayList<>();
    RecyclerView recyclerView;
    Toolbar toolbar;
    SharedPreferences spref;
    String save_text = "stickers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smileys);
        
        str_list = new ArrayList<String>();
        recyclerView = findViewById(R.id.list);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        list = new ArrayList<>();
        spref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());



        Button PickImage = (Button) findViewById(R.id.choose);
        Button TakePhoto = (Button) findViewById(R.id.take_photo);

        try {
            str_list = Load();
            if(str_list != null){

                Iterator<String> iterator = str_list.iterator();

                while(iterator.hasNext()){

                    String id = iterator.next();
                    list.add(StringToBitMap(id));

                }
            } else{str_list = new ArrayList<>();}
        } catch (Exception e){}

        if(!list.isEmpty())
        {
            sticker_adapter adapter = new sticker_adapter(list);
            recyclerView.setAdapter(adapter);
        }

        TakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try{
                    startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
            }
        });

        PickImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    try {

                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        getFaceDetail(selectedImage);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            case 2: if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                try {
                Bundle extras = imageReturnedIntent.getExtras();
                Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
                getFaceDetail(thumbnailBitmap);}catch (Exception e){}
            }
        }}


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Save();
    }

    void getFaceDetail(Bitmap drawable)
    {
        FaceDetectorOptions options = new  FaceDetectorOptions.Builder()
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        InputImage image = InputImage.fromBitmap(drawable, 0);
        FaceDetector faceDetector = FaceDetection.getClient(options);
        faceDetector.process(image).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(@NonNull List<Face> faces) {



                for (Face face : faces) {
                    Rect bounds = face.getBoundingBox();
                    try {
                        Bitmap bit = Bitmap.createBitmap(drawable,bounds.left,(int)bounds.top,bounds.width(),(int)(bounds.height()));
                        list.add(bit);
                        String string = BitMapToString(bit);
                        if(str_list == null){str_list = new ArrayList<>();}
                        str_list.add(string);
                        sticker_adapter adapter = new sticker_adapter(list);
                        recyclerView.setAdapter(adapter);}
                    catch (Exception e){}

                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    //save maked stickers to preference
    void Save()
    {
        spref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor ed = spref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(str_list);
        ed.putString(save_text, json);
        ed.apply();
    }
    ArrayList<String> Load()
    {
        spref = getSharedPreferences("pref",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = spref.getString(save_text, "");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> arrayList = gson.fromJson(json, type);
        return arrayList;
    }
}