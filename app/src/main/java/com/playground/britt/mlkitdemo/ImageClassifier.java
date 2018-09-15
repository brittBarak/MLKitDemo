package com.playground.britt.mlkitdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import java.util.List;

class ImageClassifier {

    interface ClassifierCallback {
        void onClassified(String modelTitle, List<String> topLabels, long executionTime);

    }

    private static ImageClassifier instance;
    private final Context appContext;

    static synchronized public ImageClassifier getInstance(Context context) {
        if (instance == null) {
            instance = new ImageClassifier(context.getApplicationContext());
        }
        return instance;
    }

    private ImageClassifier(Context appContext) {
        this.appContext = appContext;
    }

    public void executeLocal(Bitmap selectedImage, final ClassifierCallback callback) {
        //TODO: implement
        Toast.makeText(appContext, "To implement...", Toast.LENGTH_LONG).show();
    }

    public void executeCloud(Bitmap selectedImage, final ClassifierCallback callback) {
        //TODO: implement
        Toast.makeText(appContext, "To implement...", Toast.LENGTH_LONG).show();
    }

    public void executeCustom(Bitmap selectedImage, final ClassifierCallback callback) {
        //TODO: implement
        Toast.makeText(appContext, "To implement...", Toast.LENGTH_LONG).show();
    }
}
