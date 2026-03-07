package com.fussy.trueman;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LessonActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int currentLessonId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        dbHelper = new DatabaseHelper(this);

        TextView tvLessonTitle = findViewById(R.id.tvLessonTitle);
        TextView tvLessonContent = findViewById(R.id.tvLessonContent);
        Button btnMarkComplete = findViewById(R.id.btnMarkComplete);

        EditText etGoal = findViewById(R.id.etGoal);
        EditText etPassword = findViewById(R.id.etPassword);

        loadNextLesson(tvLessonTitle, tvLessonContent);

        btnMarkComplete.setOnClickListener(v -> {
            if (currentLessonId != -1) {
                String goalText = etGoal.getText().toString().trim();
                String passText = etPassword.getText().toString().trim();

                if (goalText.isEmpty() || passText.isEmpty()) {
                    Toast.makeText(this, "Please set a goal and secure it with a password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                markLessonCompleted(currentLessonId, goalText, passText);
                Toast.makeText(this, "Lesson and Goal secured!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadNextLesson(TextView tvTitle, TextView tvContent) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Load an incomplete lesson
        String query = "SELECT id, title, lesson_content FROM lessons WHERE id NOT IN (SELECT lesson_id FROM progress WHERE completion_status = 1) LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            currentLessonId = cursor.getInt(0);
            tvTitle.setText(cursor.getString(1));
            tvContent.setText(cursor.getString(2));
        } else {
            tvTitle.setText("All caught up!");
            tvContent.setText("You have completed all available lessons. Come back later!");
            findViewById(R.id.btnMarkComplete).setEnabled(false);
            findViewById(R.id.etGoal).setEnabled(false);
            findViewById(R.id.etPassword).setEnabled(false);
        }
        cursor.close();
    }

    private void markLessonCompleted(int lessonId, String goal, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lesson_id", lessonId);
        values.put("completion_status", 1);
        values.put("user_goal", goal);
        values.put("goal_password", password);
        db.insert("progress", null, values);
    }
}
