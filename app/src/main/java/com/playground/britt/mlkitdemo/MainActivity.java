package com.playground.britt.mlkitdemo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, ImageClassifier.ClassifierCallback {
    private final String[] filePaths =
            new String[]{"strawberry.jpg", "strawberry1.jpg", "apple1.jpg", "banana.jpg", "strawberry2.jpg",
                    "pineapple.jpg", "raspberry1.jpg", "peach.jpg", "apple.jpg"};

    private ImageView imageView;
    private Bitmap selectedImage;
    private TextView labelsOverlay;

    ImageClassifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();

        classifier = ImageClassifier.getInstance(this);

        findViewById(R.id.btn_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                labelsOverlay.setText("");
                classifier.executeLocal(selectedImage, MainActivity.this);
            }
        });

        findViewById(R.id.btn_cloud).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                labelsOverlay.setText("");
                classifier.executeCloud(selectedImage, MainActivity.this);
            }
        });

        findViewById(R.id.btn_custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labelsOverlay.setText("");
                classifier.executeCustom(selectedImage, MainActivity.this);
            }
        });

    }

    private void setUI() {
        labelsOverlay = findViewById(R.id.graphicOverlay);
        imageView = findViewById(R.id.imageView);

        setSpinner();
    }

    private void setSpinner() {
        Spinner spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_dropdown_item, getImageNames());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private List<String> getImageNames() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < filePaths.length; i++) {
            items.add("Image " + (i + 1));
        }
        return items;
    }


    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        labelsOverlay.setText("");
        selectedImage = getBitmapFromAsset(this, filePaths[position]);
        if (selectedImage != null) {
            imageView.setImageBitmap(selectedImage);
        }

    }

    @Override
    public void onClassified(String modelTitle, List<String> topLabels, long executionTime) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        builder.append(modelTitle + " - time: " + executionTime + "\n\n", boldSpan, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        if (topLabels == Collections.EMPTY_LIST || topLabels.size() == 0) {
            builder.append("No results..");
        } else {
            for (String s : topLabels) {
                builder.append(s);
                builder.append("\n");
            }

            labelsOverlay.setText(builder);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

}
