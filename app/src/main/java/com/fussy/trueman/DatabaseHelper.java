package com.fussy.trueman;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TrueMan.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_LESSONS = "lessons";
    private static final String TABLE_PROGRESS = "progress";
    private static final String TABLE_BLOCKED_WEBSITES = "blocked_websites";

    // Lessons Table Columns
    private static final String COLUMN_LESSON_ID = "id";
    private static final String COLUMN_LESSON_TITLE = "title";
    private static final String COLUMN_LESSON_CONTENT = "lesson_content";

    // Progress Table Columns
    private static final String COLUMN_PROGRESS_ID = "id";
    private static final String COLUMN_PROGRESS_LESSON_ID = "lesson_id";
    private static final String COLUMN_PROGRESS_STATUS = "completion_status";

    // Blocked Websites Table Columns
    private static final String COLUMN_BLOCKED_ID = "id";
    private static final String COLUMN_BLOCKED_DOMAIN = "domain_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Lessons Table
        String createLessonsTable = "CREATE TABLE " + TABLE_LESSONS + " (" +
                COLUMN_LESSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LESSON_TITLE + " TEXT, " +
                COLUMN_LESSON_CONTENT + " TEXT)";
        db.execSQL(createLessonsTable);

        // Create Progress Table
        String createProgressTable = "CREATE TABLE " + TABLE_PROGRESS + " (" +
                COLUMN_PROGRESS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROGRESS_LESSON_ID + " INTEGER, " +
                COLUMN_PROGRESS_STATUS + " INTEGER)";
        db.execSQL(createProgressTable);

        // Create Blocked Websites Table
        String createBlockedTable = "CREATE TABLE " + TABLE_BLOCKED_WEBSITES + " (" +
                COLUMN_BLOCKED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BLOCKED_DOMAIN + " TEXT)";
        db.execSQL(createBlockedTable);
        
        // Insert sample initial data
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Dummy lessons
        ContentValues lesson1 = new ContentValues();
        lesson1.put(COLUMN_LESSON_TITLE, "Time Management");
        lesson1.put(COLUMN_LESSON_CONTENT, "Focus on completing tasks in chunks. Use the Pomodoro technique.");
        db.insert(TABLE_LESSONS, null, lesson1);

        ContentValues lesson2 = new ContentValues();
        lesson2.put(COLUMN_LESSON_TITLE, "Online Safety");
        lesson2.put(COLUMN_LESSON_CONTENT, "Never share your personal information. Ensure safe browsing habits.");
        db.insert(TABLE_LESSONS, null, lesson2);
        
        // Initial blocked domains
        ContentValues domain1 = new ContentValues();
        domain1.put(COLUMN_BLOCKED_DOMAIN, "porn");
        db.insert(TABLE_BLOCKED_WEBSITES, null, domain1);
        
        ContentValues domain2 = new ContentValues();
        domain2.put(COLUMN_BLOCKED_DOMAIN, "xxx");
        db.insert(TABLE_BLOCKED_WEBSITES, null, domain2);
        
        ContentValues domain3 = new ContentValues();
        domain3.put(COLUMN_BLOCKED_DOMAIN, "adult");
        db.insert(TABLE_BLOCKED_WEBSITES, null, domain3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCKED_WEBSITES);
        onCreate(db);
    }

    // --- Data Access Methods ---

    public List<String> getAllBlockedDomains() {
        List<String> domains = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_BLOCKED_DOMAIN + " FROM " + TABLE_BLOCKED_WEBSITES, null);
        if (cursor.moveToFirst()) {
            do {
                domains.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return domains;
    }

    public void addBlockedDomain(String domain) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BLOCKED_DOMAIN, domain);
        db.insert(TABLE_BLOCKED_WEBSITES, null, values);
    }
}
