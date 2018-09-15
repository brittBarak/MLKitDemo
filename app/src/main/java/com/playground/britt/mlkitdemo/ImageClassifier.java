package com.playground.britt.mlkitdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;

class ImageClassifier {

    public static final int RESULTS_TO_SHOW = 3;
    public static final float CONFIDENCE_THRESHOLD = 0.75f;

    interface ClassifierCallback {
        void onClassified(String modelTitle, List<String> topLabels, long executionTime);
    }

    private static ImageClassifier instance;
    private final Context appContext;
    private LocalClassifier localClassifier;

    private List<String> resultLabels = new ArrayList<>();

    private OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            handleError(e);
        }
    };

    private Comparator<FirebaseVisionLabel> localLabelComparator = new Comparator<FirebaseVisionLabel>() {
        @Override
        public int compare(FirebaseVisionLabel label1, FirebaseVisionLabel label2) {
            return (int) (label1.getConfidence() - label2.getConfidence());
        }
    };

    static synchronized public ImageClassifier getInstance(Context context) {
        if (instance == null) {
            instance = new ImageClassifier(context.getApplicationContext());
        }
        return instance;
    }

    private ImageClassifier(Context appContext) {
        this.appContext = appContext;
        this.localClassifier = new LocalClassifier();
    }

    public void executeLocal(Bitmap bitmap, final ClassifierCallback callback) {
        final long start = System.currentTimeMillis();

        OnSuccessListener<List<FirebaseVisionLabel>> successListener = new OnSuccessListener<List<FirebaseVisionLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionLabel> labels) {
                //4.1. process output
                processLocalResult(labels, callback, start);
            }
        };

        localClassifier.execute(bitmap, successListener, failureListener);
    }

    private void processLocalResult(List<FirebaseVisionLabel> labels, ClassifierCallback callback, long start) {
        labels.sort(localLabelComparator);
        resultLabels.clear();

        FirebaseVisionLabel label;
        for (int i = 0; i < Math.min(RESULTS_TO_SHOW, labels.size()); ++i) {
            label = labels.get(i);
            resultLabels.add(label.getLabel() + ":" + label.getConfidence());
        }
        callback.onClassified("Local Model", resultLabels, System.currentTimeMillis() - start);
    }

    public void executeCloud(Bitmap selectedImage, final ClassifierCallback callback) {
        //TODO: implement
        Toast.makeText(appContext, "To implement...", Toast.LENGTH_LONG).show();
    }

    public void executeCustom(Bitmap selectedImage, final ClassifierCallback callback) {
        //TODO: implement
        Toast.makeText(appContext, "To implement...", Toast.LENGTH_LONG).show();
    }

    private void handleError(Exception e) {
        Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }
}
