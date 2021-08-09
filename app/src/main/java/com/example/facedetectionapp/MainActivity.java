package com.example.facedetectionapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button choose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        choose = findViewById(R.id.button);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,"choose Image"),121);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 121){
            imageView.setImageURI(data.getData());
           InputImage image;

           try{
               Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
               Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
               Canvas canvas = new Canvas(mutableBitmap);
               image = InputImage.fromFilePath(getApplicationContext(),data.getData());

               FaceDetectorOptions highAccuracyOpts =
                       new FaceDetectorOptions.Builder()
                               .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                               .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                               .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                               .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                               .build();


               FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);

               Task<List<Face>> result =
                       detector.process(image)
                               .addOnSuccessListener(
                                       new OnSuccessListener<List<Face>>() {
                                           @Override
                                           public void onSuccess(List<Face> faces) {
                                               // Task completed successfully
                                               // ...
                                               for (Face face : faces) {
                                                   Rect bounds = face.getBoundingBox();

                                                   Paint p = new Paint();
                                                   p.setColor(Color.GREEN);
                                                   p.setStyle(Paint.Style.STROKE);
                                                   canvas.drawRect(bounds,p);
                                                   imageView.setImageBitmap(mutableBitmap);

                                                   float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                   float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                   // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                   // nose available):
                                                   FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                                   if (leftEar != null) {
                                                       PointF leftEarPos = leftEar.getPosition();
                                                   }

                                                   // If contour detection was enabled:
                                                   //List<PointF> leftEyeContour =
                                                      //     face.getContour(FaceContour.LEFT_EYE).getPoints();
                                                  // List<PointF> upperLipBottomContour =
                                                       //    face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();

                                                   // If classification was enabled:
                                                   if (face.getSmilingProbability() != null) {
                                                       float smileProb = face.getSmilingProbability();
                                                   }
                                                   if (face.getRightEyeOpenProbability() != null) {
                                                       float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                   }

                                                   // If face tracking was enabled:
                                                   if (face.getTrackingId() != null) {
                                                       int id = face.getTrackingId();
                                                   }
                                               }

                                           }
                                       })
                               .addOnFailureListener(
                                       new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {
                                               // Task failed with an exception
                                               // ...
                                           }
                                       });

           }catch (IOException e){
               e.printStackTrace();
           }
        }
    }
}
