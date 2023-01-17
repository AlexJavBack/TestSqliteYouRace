package com.example.testyourraceintes;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH; // full path to the database
    private final static String DB_NAME = "db.db";
    private static final int SCHEMA = 1; // database version
    // name tables in DB
    public static final String TABLE_ANSWERS = "Answers";
    public static final String TABLE_QUESTION_TEXT = "QuestionText";
    public static final String TABLE_RESULT = "Results";
    public static final String TABLE_RESULT_EMPTY = "ResultsEmpty";
    // name column Answers
    public static final String COLUMN_ANSWERS_ID = "Id";
    public static final String COLUMN_ANSWERS_RACE_NAME = "RaceName";
    public static final String COLUMN_ANSWERS_NUM_QUESTION = "NumQuestion";
    public static final String COLUMN_ANSWERS_ANSWER_TEXT = "AnswerText";
    public static final String COLUMN_ANSWERS_ID_RADIO_BUTTON = "IdRadioButton";
    // name column QuestionText
    public static final String COLUMN_QUESTION_TEXT_ID = "Id";
    public static final String COLUMN_QUESTION_TEXT_NUM_QUESTION = "NumQuestion";
    public static final String COLUMN_QUESTION_TEXT_TEXT_QUESTION = "TextQuestion";
    public static final String COLUMN_QUESTION_TEXT_IMAGES = "Images";
    // name column Results
    public static final String COLUMN_RESULT_ID = "Id";
    public static final String COLUMN_RESULT_RACE_NAME = "RaceName";
    public static final String COLUMN_RESULT_RACE_VALUE = "RaceValue";
    public static final String COLUMN_RESULT_RACE_IMAGE = "RaceImage";
    // name column ResultsEmpty
    public static final String COLUMN_RESULT_EMPTY_ID = "Id";
    public static final String COLUMN_RESULT_EMPTY_RACE_NAME = "RaceNameE";
    public static final String COLUMN_RESULT_EMPTY_RACE_VALUE = "RaceValue";
    private Context myContext;


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        this.myContext=context;
        DB_PATH =context.getFilesDir().getPath() + DB_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
    }

    // Getting the finished database from a file
    public void create_db(){
        File file = new File(DB_PATH);
        if (!file.exists()) {
            //get local database as stream
            try(InputStream myInput = myContext.getAssets().open(DB_NAME);
                // Opening an empty database
                OutputStream myOutput = new FileOutputStream(DB_PATH)) {

                // copy data byte by byte
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
            }
            catch(IOException ex){
                Log.d("DatabaseHelper", ex.getMessage());
            }
        }
    }
    // oped DB in program code
    public SQLiteDatabase open()throws SQLException {

        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }
    // get text for field "Answer"
    public String getTextAnswer(SQLiteDatabase db, int radioButtonNumber, int questionNumber) {
        Cursor answer = db.rawQuery("select " + DatabaseHelper.COLUMN_ANSWERS_ANSWER_TEXT +
                " from " + DatabaseHelper.TABLE_ANSWERS + " Where " + DatabaseHelper.COLUMN_ANSWERS_ID_RADIO_BUTTON +
                " = " + radioButtonNumber + " AND " + DatabaseHelper.COLUMN_ANSWERS_NUM_QUESTION +
                " = " + questionNumber, null);
        answer.moveToFirst();
        String result = answer.getString(0);
        answer.close();
        return result;
    }
    // Complete add or remove number ball depending on choice buttons Next and Cancel
    public void addEmptyResult(SQLiteDatabase db, String answerText, int numQuestion, int importanceQuestion, boolean itIsSum){
        Cursor cursor;
        if(itIsSum){
            cursor = db.rawQuery("select " + DatabaseHelper.COLUMN_ANSWERS_RACE_NAME + " from "
                    + DatabaseHelper.TABLE_ANSWERS + " Where "
                    + DatabaseHelper.COLUMN_ANSWERS_ANSWER_TEXT + " = '" + answerText + "' and "
                    + DatabaseHelper.COLUMN_ANSWERS_NUM_QUESTION + " = " + numQuestion,
                    null);
            if (cursor.moveToFirst()) {
                do {
                    db.execSQL("UPDATE " + DatabaseHelper.TABLE_RESULT_EMPTY + " SET " +
                            DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + " = " +
                            DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + "+" + importanceQuestion
                            + " Where " + DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_NAME + " = '" +
                            cursor.getString(0) + "'");
                }
                while (cursor.moveToNext());
            }
        }
        else {
            cursor = db.rawQuery("select " + DatabaseHelper.COLUMN_ANSWERS_RACE_NAME + " from "
                    + DatabaseHelper.TABLE_ANSWERS + " Where " +
                    DatabaseHelper.COLUMN_ANSWERS_ANSWER_TEXT + " = '" + answerText + "' and "
                    + DatabaseHelper.COLUMN_ANSWERS_NUM_QUESTION + " = " + numQuestion,
                    null);
            if (cursor.moveToFirst()) {
                do {
                    db.execSQL("UPDATE " + DatabaseHelper.TABLE_RESULT_EMPTY + " SET " +
                            DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + " = "
                            + DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + "-" +
                            importanceQuestion + " Where " +
                            DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_NAME + " = '" +
                            cursor.getString(0) + "'");
                }
                while (cursor.moveToNext());
            }
        }
        cursor.close();
    }
    // rewrite the data from the table to the table the final result and reset the data from
    // the draft
    public void endTesting(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select " + DatabaseHelper.COLUMN_ANSWERS_RACE_NAME + " from " + DatabaseHelper.TABLE_ANSWERS, null);
        if(cursor.moveToFirst()) {
            do {
                db.execSQL("UPDATE " + DatabaseHelper.TABLE_RESULT + " SET " + DatabaseHelper.COLUMN_RESULT_RACE_VALUE + " = (Select " + DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + " From " + DatabaseHelper.TABLE_RESULT_EMPTY + " Where " + DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_NAME + " = '" + cursor.getString(0) + "') where " + DatabaseHelper.COLUMN_RESULT_RACE_NAME + " = '" + cursor.getString(0) + "'");
            }
            while ((cursor.moveToNext()));
        }
        db.execSQL("Update " + DatabaseHelper.TABLE_RESULT_EMPTY + " SET " + DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + " = 0 ");
        cursor.close();
    }
    // reset all selections in case of closing the test
    public void closeTest(SQLiteDatabase db){
        db.execSQL("Update " + DatabaseHelper.TABLE_RESULT_EMPTY + " SET " + DatabaseHelper.COLUMN_RESULT_EMPTY_RACE_VALUE + " = 0 ");
    }


}
