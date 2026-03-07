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

public class NutritionActivity extends AppCompatActivity {

    private TextView tvFruitName;
    private TextView tvMacros;
    private Button btnNextFact;

    private JSONArray fruitsArray = null;
    private Random random = new Random();

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        tvFruitName = findViewById(R.id.tvFruitName);
        tvMacros = findViewById(R.id.tvMacros);
        btnNextFact = findViewById(R.id.btnNextFact);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        btnNextFact.setOnClickListener(v -> {
            if (fruitsArray != null && fruitsArray.length() > 0) {
                showRandomFruit();
            } else {
                tvFruitName.setText("Loading...");
                tvMacros.setText("");
                btnNextFact.setEnabled(false);
                fetchNutrition();
            }
        });

        fetchNutrition();
    }

    private void fetchNutrition() {
        executorService.execute(() -> {
            try {
                URL url = new URL("https://www.fruityvice.com/api/fruit/all");
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

                final JSONArray results = new JSONArray(response.toString());

                mainHandler.post(() -> {
                    fruitsArray = results;
                    btnNextFact.setEnabled(true);
                    showRandomFruit();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(NutritionActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    tvFruitName.setText("Failed to load nutrition data. Check intentet connectivity");
                    btnNextFact.setEnabled(true);
                });
            }
        });
    }

    private void showRandomFruit() {
        try {
            int index = random.nextInt(fruitsArray.length());
            JSONObject fruit = fruitsArray.getJSONObject(index);

            String name = fruit.getString("name");
            JSONObject nutritions = fruit.getJSONObject("nutritions");

            double calories = nutritions.getDouble("calories");
            double fat = nutritions.getDouble("fat");
            double protein = nutritions.getDouble("protein");
            double carbs = nutritions.getDouble("carbohydrates");
            double sugar = nutritions.getDouble("sugar");

            tvFruitName.setText(name + " 🍎🍏🍌🍇🍉");

            String macros = String.format(
                    "Calories: %.1f kcal\nFat: %.1f g\nProtein: %.1f g\nCarbohydrates: %.1f g\nSugar: %.1f g",
                    calories, fat, protein, carbs, sugar);

            tvMacros.setText(macros);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
