package com.fussy.trueman;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLessons = findViewById(R.id.btnLessons);
        Button btnProgress = findViewById(R.id.btnProgress);
        Button btnBrowser = findViewById(R.id.btnBrowser);
        Button btnParental = findViewById(R.id.btnParental);

        btnLessons.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LessonActivity.class)));
        btnProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProgressActivity.class)));
        btnBrowser.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BrowserActivity.class)));
        btnParental
                .setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ParentalControlActivity.class)));

        Button btnItStudies = findViewById(R.id.btnItStudies);
        Button btnExercise = findViewById(R.id.btnExercise);
        Button btnNutrition = findViewById(R.id.btnNutrition);

        btnItStudies.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ItStudiesActivity.class)));
        btnExercise.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ExerciseActivity.class)));
        btnNutrition.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NutritionActivity.class)));
    }
}
