package com.fussy.trueman;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        dbHelper = new DatabaseHelper(this);

        TextView tvCompleted = findViewById(R.id.tvCompletedLessons);
        TextView tvTotal = findViewById(R.id.tvTotalLessons);

        loadProgress(tvCompleted, tvTotal);
    }

    private void loadProgress(TextView tvCompleted, TextView tvTotal) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get total lessons
        Cursor cursorTotal = db.rawQuery("SELECT COUNT(*) FROM lessons", null);
        int total = 0;
        if (cursorTotal.moveToFirst()) {
            total = cursorTotal.getInt(0);
        }
        cursorTotal.close();

        // Get completed lessons
        Cursor cursorCompleted = db.rawQuery("SELECT COUNT(*) FROM progress WHERE completion_status = 1", null);
        int completed = 0;
        if (cursorCompleted.moveToFirst()) {
            completed = cursorCompleted.getInt(0);
        }
        cursorCompleted.close();

        tvCompleted.setText("Lessons Completed: " + completed);
        tvTotal.setText("Total Lessons: " + total);
    }
}
