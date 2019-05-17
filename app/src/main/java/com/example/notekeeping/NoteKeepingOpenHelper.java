package com.example.notekeeping;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteKeepingOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NoteKeeping.db";
    public static final int DATABASE_VERSION = 1;

    public NoteKeepingOpenHelper(@Nullable Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( NotekeepingDatabaseContract.CourseInfoEntry.SQL_CREATE_TABLE );
        db.execSQL( NotekeepingDatabaseContract.NoteInfoEntry.SQL_CREATE_TABLE );

        DatabaseDataWorker worker = new DatabaseDataWorker( db );
        worker.insertCourses();
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
