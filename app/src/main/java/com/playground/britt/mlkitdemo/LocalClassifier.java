package com.playground.britt.mlkitdemo;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;


public class LocalClassifier {
    FirebaseVisionImage image;

    private FirebaseVisionLabelDetectorOptions localDetectorOptions =
            new FirebaseVisionLabelDetectorOptions.Builder()
                    .setConfidenceThreshold(ImageClassifier.CONFIDENCE_THRESHOLD)
                    .build();

    //1.1. setup detector
    private FirebaseVisionLabelDetector detector = FirebaseVision.getInstance().getVisionLabelDetector(localDetectorOptions);

    public void execute(Bitmap bitmap, OnSuccessListener successListener, OnFailureListener failureListener) {
        //2.1. process input
        image = FirebaseVisionImage.fromBitmap(bitmap);

        //3.1. run model
        detector.detectInImage(image)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

}
