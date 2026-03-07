package com.fussy.trueman;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.text.Html;

public class ExerciseActivity extends AppCompatActivity {

    private TextView tvExerciseName;
    private TextView tvExerciseDesc;
    private Button btnNextExercise;

    private JSONArray exercisesArray = null;
    private Random random = new Random();

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvExerciseDesc = findViewById(R.id.tvExerciseDesc);
        btnNextExercise = findViewById(R.id.btnNextExercise);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        btnNextExercise.setOnClickListener(v -> {
            if (exercisesArray != null && exercisesArray.length() > 0) {
                showRandomExercise();
            } else {
                tvExerciseName.setText("Loading...");
                tvExerciseDesc.setText("");
                btnNextExercise.setEnabled(false);
                fetchExercises();
            }
        });

        // Initial load
        fetchExercises();
    }

    private void fetchExercises() {
        executorService.execute(() -> {
            try {
                // Fetch some exercises in English (language=2)
                URL url = new URL("https://wger.de/api/v2/exerciseinfo/?language=2&limit=20");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                final JSONArray results = jsonObject.getJSONArray("results");

                mainHandler.post(() -> {
                    exercisesArray = results;
                    btnNextExercise.setEnabled(true);
                    showRandomExercise();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(ExerciseActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    tvExerciseName.setText("Failed to load exercise");
                    btnNextExercise.setEnabled(true);
                });
            }
        });
    }

    private void showRandomExercise() {
        try {
            int index = random.nextInt(exercisesArray.length());
            JSONObject exercise = exercisesArray.getJSONObject(index);

            String name = exercise.getString("name");
            String desc = exercise.getString("description");

            tvExerciseName.setText(name);
            tvExerciseDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_COMPACT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
