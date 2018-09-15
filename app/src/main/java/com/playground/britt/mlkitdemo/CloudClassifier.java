package com.playground.britt.mlkitdemo;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;

public class CloudClassifier {
    FirebaseVisionImage image;

    private FirebaseVisionCloudDetectorOptions cloudDetectorOptions =
            new FirebaseVisionCloudDetectorOptions.Builder()
                    .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
                    .setMaxResults(ImageClassifier.RESULTS_TO_SHOW)
                    .build();


    //1.2. setup detector
    private FirebaseVisionCloudLabelDetector detector =
            FirebaseVision.getInstance().getVisionCloudLabelDetector(cloudDetectorOptions);

    public void execute(Bitmap bitmap, OnSuccessListener<List<FirebaseVisionCloudLabel>> successListener, OnFailureListener failureListener) {
        //2.2. process input
        image = FirebaseVisionImage.fromBitmap(bitmap);

        //3.2. run model
        detector.detectInImage(image)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}
