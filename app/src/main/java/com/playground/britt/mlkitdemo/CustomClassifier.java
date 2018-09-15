package com.playground.britt.mlkitdemo;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource;
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource;
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class CustomClassifier {

    private static final String LABEL_PATH = "labels.txt";

    private static final String HOSTED_MODEL_NAME = "mobilenet_v1_224_quant";
    private static final String LOCAL_MODEL_NAME = "local_mobilenet_v1_224_quant.tflite";

    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
    private final Context appContext;
    private OnFailureListener failureListener;
    ByteBuffer imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);


    private List<String> labelList;
    private FirebaseModelInputOutputOptions inputOutputOptions;
    private FirebaseModelInterpreter interpreter;
    private ArrayList<LabelAndProb> resultList = new ArrayList<>();
    private FirebaseModelInputs inputs;
    ;

    public CustomClassifier(Context appContext, OnFailureListener failureListener) {
        this.appContext = appContext;
        this.failureListener = failureListener;

        try {
            //1.3. setup detector
            prepareInputOutputFormat();
            configureLocalModel();
            configureCloudModel();
            setClassifier();
        } catch (FirebaseMLException e) {
            failureListener.onFailure(e);
        }
    }

    private void prepareInputOutputFormat() throws FirebaseMLException {
        //what are the input and the output format to expect?
        labelList = loadLabelsFromFile();

        int[] inputDims = {DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE};
        int[] outputDims = {DIM_BATCH_SIZE, labelList.size()};

        inputOutputOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.BYTE, inputDims)
                        .setOutputFormat(0, FirebaseModelDataType.BYTE, outputDims)
                        .build();


    }

    private void configureLocalModel() {
        FirebaseLocalModelSource localModelSource =
                new FirebaseLocalModelSource.Builder("asset")
                        .setAssetFilePath(LOCAL_MODEL_NAME).build();
        FirebaseModelManager.getInstance().registerLocalModelSource(localModelSource);
    }

    private void configureCloudModel() {
        FirebaseModelDownloadConditions conditions =
                new FirebaseModelDownloadConditions.Builder()
                        .requireWifi()
                        .build();

        FirebaseCloudModelSource cloudSource = new FirebaseCloudModelSource.Builder
                (HOSTED_MODEL_NAME)
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();

        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource);
    }

    private void setClassifier() throws FirebaseMLException {
        FirebaseModelOptions modelOptions = new FirebaseModelOptions.Builder()
                .setCloudModelName(HOSTED_MODEL_NAME)
                .setLocalModelName("asset")
                .build();
        interpreter = FirebaseModelInterpreter.getInstance(modelOptions);
    }

    private List<String> loadLabelsFromFile() {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(appContext.getAssets().open(LABEL_PATH)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labelList.add(line);
            }
        } catch (IOException e) {
            failureListener.onFailure(e);
        }
        return labelList;
    }


    public void execute(Bitmap bitmap, final OnSuccessListener<List<LabelAndProb>> successListener, final OnFailureListener failureListener) {
        if (interpreter == null) {
            failureListener.onFailure(new Exception("Custom interpreter wasn't loaded yet"));
            return;
        }

        try {
            //2.3. process input
            processInput(bitmap);

            OnSuccessListener<FirebaseModelOutputs> successListenerWrapper = new OnSuccessListener<FirebaseModelOutputs>() {
                @Override
                public void onSuccess(FirebaseModelOutputs result) {
                    //4.3.1 process output (for simplicity)
                    processOutput(result);
                    successListener.onSuccess(resultList);
                }
            };

            //3.3. run model
            interpreter.run(inputs, inputOutputOptions)
                    .addOnSuccessListener(successListenerWrapper)
                    .addOnFailureListener(failureListener);
        } catch (FirebaseMLException e) {
            failureListener.onFailure(e);
        }

    }

    private FirebaseModelInputs processInput(Bitmap bitmap) throws FirebaseMLException {
        imgData = convertBitmapToByteBuffer(bitmap);
        if (imgData == null) return null;

        inputs = new FirebaseModelInputs.Builder()
                .add(imgData)
                .build();

        return inputs;
    }

    private void processOutput(FirebaseModelOutputs result) {
        byte[][] output = result.getOutput(0);
        byte[] probabilities = output[0];

        resultList.clear();

        float probability;
        for (int i = 0; i < labelList.size(); i++) {
            probability = convertByteToFloat(probabilities[i]);
            if (probability >= ImageClassifier.CONFIDENCE_THRESHOLD) {
                resultList.add(new LabelAndProb(labelList.get(i), probability));
            }
        }
    }


    private float convertByteToFloat(byte b) {
        return (b & 0xff) / 255.0f;
    }

    private synchronized ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        imgData.order(ByteOrder.nativeOrder());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true);
        imgData.rewind();
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());
        // Convert the image to int points.
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imgData.put((byte) ((val >> 16) & 0xFF));
                imgData.put((byte) ((val >> 8) & 0xFF));
                imgData.put((byte) (val & 0xFF));
            }
        }
        return imgData;
    }

}
