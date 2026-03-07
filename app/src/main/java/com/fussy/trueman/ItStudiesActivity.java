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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.text.Html;

public class ItStudiesActivity extends AppCompatActivity {

    private TextView tvQuestion;
    private TextView tvAnswer;
    private Button btnNextQuestion;

    private String currentAnswer = "";
    private boolean isAnswerShowing = false;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_it_studies);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        btnNextQuestion.setOnClickListener(v -> {
            if (isAnswerShowing) {
                // Fetch next
                tvQuestion.setText("Loading...");
                tvAnswer.setText("");
                btnNextQuestion.setText("Loading...");
                btnNextQuestion.setEnabled(false);
                fetchITQuestion();
            } else {
                // Show Answer
                tvAnswer.setText("Answer: " + currentAnswer);
                btnNextQuestion.setText("Next Subject/Question");
                isAnswerShowing = true;
            }
        });

        fetchITQuestion();
    }

    private void fetchITQuestion() {
        executorService.execute(() -> {
            try {
                URL url = new URL("https://opentdb.com/api.php?amount=1&category=18&type=multiple");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                int responseCode = jsonObject.getInt("response_code");

                if (responseCode == 0) {
                    JSONArray results = jsonObject.getJSONArray("results");
                    JSONObject questionData = results.getJSONObject(0);

                    String qText = Html.fromHtml(questionData.getString("question"), Html.FROM_HTML_MODE_COMPACT)
                            .toString();
                    String aText = Html.fromHtml(questionData.getString("correct_answer"), Html.FROM_HTML_MODE_COMPACT)
                            .toString();

                    mainHandler.post(() -> {
                        tvQuestion.setText(qText);
                        currentAnswer = aText;
                        isAnswerShowing = false;
                        btnNextQuestion.setText("Show Answer");
                        btnNextQuestion.setEnabled(true);
                    });
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(ItStudiesActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        btnNextQuestion.setText("Retry");
                        btnNextQuestion.setEnabled(true);
                        isAnswerShowing = true;
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    Toast.makeText(ItStudiesActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    tvQuestion.setText("Failed to load question. Check your internet.");
                    btnNextQuestion.setText("Retry");
                    btnNextQuestion.setEnabled(true);
                    isAnswerShowing = true;
                });
            }
        });
    }
}
