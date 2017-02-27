package com.android.notebook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.notebook.model.Note;

import java.util.ArrayList;

public class NoteDatabaseHelper extends SQLiteOpenHelper {

    private static NoteDatabaseHelper noteDatabaseHelper;
    public static final String DATABASE_NAME = "noteDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_Note = "note";

    private static final String NOTE_ID = "id";
    private static final String NOTE_TITLE = "title";
    private static final String NOTE_CONTENT = "content";
    private static final String NOTE_TIME = "creationTime";

    NoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized NoteDatabaseHelper getNoteItemDatabase(Context context) {
        if (noteDatabaseHelper == null) {
            noteDatabaseHelper = new NoteDatabaseHelper(context);
        }
        return noteDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TODO_TABLE = "CREATE TABLE " + TABLE_Note +
                "(" +
                NOTE_ID + " INTEGER PRIMARY KEY," +
                NOTE_TITLE + " TEXT, " + NOTE_CONTENT + " TEXT," +
                NOTE_TIME + " INTEGER)";
        Log.d("Todo", CREATE_TODO_TABLE);
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_Note);
            onCreate(db);
        }
    }

    public void addNoteItem(Note note) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(NOTE_TITLE, note.getTitle());
            values.put(NOTE_CONTENT, note.getContent());
            values.put(NOTE_TIME, note.getTimeOfAddition());
            long id = sqLiteDatabase.insertOrThrow(TABLE_Note, null, values);
            Log.d("InsertedID", String.valueOf(id));
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void updateNoteItem(Note note) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(NOTE_TITLE, note.getTitle());
        values.put(NOTE_CONTENT, note.getContent());
        values.put(NOTE_TIME, note.getTimeOfAddition());
        try {
            int rows = sqLiteDatabase.update(TABLE_Note, values, NOTE_TIME + " = ?", new String[]{String.valueOf(note.getTimeOfAddition())});
            Log.d("updateRows", String.valueOf(rows));
            if (rows == 1) {
                String toDoUpdateQuery = String.format("SELECT * FROM %s WHERE %s = ?",
                        TABLE_Note, NOTE_TIME);
                Cursor cursor = sqLiteDatabase.rawQuery(toDoUpdateQuery, new String[]{
                        String.valueOf(note.getTimeOfAddition())});
                Log.d("usersSelectQuery", toDoUpdateQuery);
                try {
                    if (cursor.moveToFirst()) {
                        sqLiteDatabase.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                        sqLiteDatabase.endTransaction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteNoteFromDatabase(Note note) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            String toDoDeleteQuery = String.format("DELETE FROM %s WHERE %s = ?",
                    TABLE_Note, NOTE_TIME);
            Log.d("DELETE", toDoDeleteQuery);
            int rowsDeleted = sqLiteDatabase.delete(TABLE_Note, NOTE_TIME + " = ?", new String[]{String.valueOf(note.getTimeOfAddition())});
            Log.d("Rows", String.valueOf(rowsDeleted));
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Note> getNoteFromDatabase() {
        ArrayList<Note> noteList = new ArrayList<>();
        String SELECTION_QUERY = String.format("SELECT * FROM %s", TABLE_Note);
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(SELECTION_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)),
                            cursor.getString(cursor.getColumnIndex(NOTE_CONTENT)),
                            cursor.getLong(cursor.getColumnIndex(NOTE_TIME)));
                    noteList.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        Log.d("ToDoLisSize", String.valueOf(noteList.size()));
        return noteList;
    }
}
