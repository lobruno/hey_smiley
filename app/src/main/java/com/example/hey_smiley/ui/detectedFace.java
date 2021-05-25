package com.example.hey_smiley.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hey_smiley.MainActivity;
import com.example.hey_smiley.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class detectedFace extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 2;
    ArrayList<Face> detectedFaces = new ArrayList<>();
    Button detect;
    ImageView img;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_face);
        auth = FirebaseAuth.getInstance();

        detect = findViewById(R.id.but_ok);
        img = findViewById(R.id.image_ava);

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(img.getDrawable() != null){
                getFaceDetail(img);}
            }
        });

        Button PickImage = (Button) findViewById(R.id.choose);
        Button TakePhoto = (Button) findViewById(R.id.take_photo);

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
                        img.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            case 2: if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                Bundle extras = imageReturnedIntent.getExtras();
                Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
                img.setImageBitmap(thumbnailBitmap);
            }
        }}





    void getFaceDetail(ImageView view)
    {
        BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
        FaceDetectorOptions options = new  FaceDetectorOptions.Builder()
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        InputImage image = InputImage.fromBitmap(drawable.getBitmap(), 0);
        FaceDetector faceDetector = FaceDetection.getClient(options);
        faceDetector.process(image).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(@NonNull List<Face> faces) {
                if(faces != null)
                    detectedFaces.addAll(faces);


                for (Face face : faces) {
                    if (face.getSmilingProbability() != null) {
                        float smileProb = face.getSmilingProbability();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("img", Float.toString(smileProb));
                        reference.child(userId).child("img").setValue(smileProb);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
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
}