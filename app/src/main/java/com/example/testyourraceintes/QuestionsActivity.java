package com.example.testyourraceintes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class QuestionsActivity extends AppCompatActivity {
    private TextView questionText;
    private Button cancel, next;
    private ImageView question_image;
    private RadioGroup radioGroup;
    private RadioButton answerOne;
    private RadioButton answerTwo;
    private RadioButton answerThree;
    private int questionNum = 1;
    private String answerText;
    private int imageIntId;
    private int importanceQuestion;
    private final List<String> answersTxt = new ArrayList<>();
    private final List<String> previousAnswer = new ArrayList<>();
    // In the test questions have different degrees of importance.
    // To determine it, I created a two-dimensional array where the row is responsible for
    // the question, and the column for the number of points for the answer.
    private final int[][] iqList = new int[][]
            {{7, 11, 14, 17, 19, 20}, {1, 2, 3, 8, 10, 12, 15, 16, 18}, {4, 5, 6, 9, 13}};
    private DatabaseHelper databaseHelper;

    private SQLiteDatabase db;
    private Cursor questionCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        questionText = findViewById(R.id.question_text);
        cancel = findViewById(R.id.cancel);
        next = findViewById(R.id.next);
        question_image = findViewById(R.id.question_image);
        answerOne = findViewById(R.id.radioButtonOne);
        answerTwo = findViewById(R.id.radioButtonTwo);
        answerThree = findViewById(R.id.radioButtonThree);
        radioGroup = findViewById(R.id.radioGroup);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        // create the database. In SQLite, in order to be able to use a database from another source,
        // it must be copied into the android application data so that it is created there.
        databaseHelper.create_db();
    }

    @Override
    public void onResume() {
        super.onResume();
        // open database for use
        db = databaseHelper.open();
        //db.execSQL("delete from " + DatabaseHelper.TABLE_LOCALRU);
        //We receive data in the form of a cursor.
        // That is, we get an array with all the data that the cursor
        // will receive based on the SQL query
        questionCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE_QUESTION_TEXT,
                null);
        //fill in the array of possible answers. The variable
        // "I" is used to designate the radio button where the search text is taken from,
        // then it is put into an array.
        for (int i = 1; i != 4; i++) {
            answersTxt.add(databaseHelper.getTextAnswer(db, i, questionNum));
        }
        // Get the right data for a specific question. Initially,
        // the moveToFirst expression was used, but in order to avoid problems associated with data
        // loss when the application was minimized, it was decided to change the code and introduce
        // correctness to the desired question.
        if (questionCursor.moveToPosition(questionNum - 1)) {
            questionText.setText(databaseHelper.getLocalText(db, questionCursor.getString(2)));
            imageIntId = getResources().getIdentifier(questionCursor.getString(3),
                    "drawable", getPackageName());
            question_image.setImageResource(imageIntId);
        }
        imageIntId = getResources().getIdentifier("R.drawable.question1","drawable", getPackageName());
        answerOne.setText(databaseHelper.getLocalText(db,answersTxt.get(0)));
        answerTwo.setText(databaseHelper.getLocalText(db,answersTxt.get(1)));
        answerThree.setText(databaseHelper.getLocalText(db,answersTxt.get(2)));

        // processing of clicking on a radio button in a radio group.
        // From here we get which answer the user chose
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonOne) {
                //answerText = (String) answerOne.getText();
                answerText = answersTxt.get(0);
                next.setEnabled(true);
            } else if (checkedId == R.id.radioButtonTwo) {
                //answerText = (String) answerTwo.getText();
                answerText = answersTxt.get(1);
                next.setEnabled(true);
            } else if (checkedId == R.id.radioButtonThree) {
                //answerText = (String) answerThree.getText();
                answerText = answersTxt.get(2);
                next.setEnabled(true);
            }
        });

        next.setOnClickListener(v -> {
            // if it was the last question
            if (questionCursor.isLast()) {
                databaseHelper.addEmptyResult(db, answerText, questionNum,
                        defineImportanceQuestion(), true);
                databaseHelper.endTesting(db);
                Intent intent = new Intent(QuestionsActivity.this, ResultTestActivity.class);
                startActivity(intent);
            } else {
                previousAnswer.add(answerText);
                databaseHelper.addEmptyResult(db, answerText, questionNum,
                        defineImportanceQuestion(), true);
                radioGroup.clearCheck();
                next.setEnabled(false);
                questionCursor.moveToNext();
                questionText.setText(databaseHelper.getLocalText(db, questionCursor.getString(2)));
                imageIntId = getResources().getIdentifier(questionCursor.getString(3),
                        "drawable", getPackageName());
                question_image.setImageResource(imageIntId);
                questionNum++;
                translateInterface(databaseHelper,db,questionNum);
                answersTxt.clear();
                for (int i = 1; i != 4; i++) {
                    answersTxt.add(databaseHelper.getTextAnswer(db, i, questionNum));
                }
                translateInterface(databaseHelper,db,questionNum);
                answerOne.setText(databaseHelper.getLocalText(db,answersTxt.get(0)));
                answerTwo.setText(databaseHelper.getLocalText(db,answersTxt.get(1)));
                answerThree.setText(databaseHelper.getLocalText(db,answersTxt.get(2)));
            }
        });
        cancel.setOnClickListener(v -> {
            if (questionCursor.isFirst()) {
                databaseHelper.closeTest(db);
                Intent intent = new Intent(QuestionsActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                questionCursor.moveToPrevious();
                questionText.setText(databaseHelper.getLocalText(db, questionCursor.getString(2)));
                imageIntId = getResources().getIdentifier(questionCursor.getString(3),
                        "drawable", getPackageName());
                question_image.setImageResource(imageIntId);
                questionNum--;
                translateInterface(databaseHelper,db,questionNum);
                answersTxt.clear();
                for (int i = 1; i != 4; i++) {
                    answersTxt.add(databaseHelper.getTextAnswer(db, i, questionNum));
                }
                answerOne.setText(databaseHelper.getLocalText(db,answersTxt.get(0)));
                answerTwo.setText(databaseHelper.getLocalText(db,answersTxt.get(1)));
                answerThree.setText(databaseHelper.getLocalText(db,answersTxt.get(2)));
                databaseHelper.addEmptyResult(db, previousAnswer.get(questionNum - 1), questionNum,
                        defineImportanceQuestion(), false);
                previousAnswer.remove(questionNum - 1);
                next.setEnabled(false);
                radioGroup.clearCheck();
            }
        });
    }

    @Override
    public void onDestroy() {
        db = databaseHelper.open();
        databaseHelper.closeTest(db);
        db.close();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        db.close();
        super.onStop();
    }


    private int defineImportanceQuestion() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < iqList[i].length; j++) {
                if (questionNum == iqList[i][j]) {
                    importanceQuestion = i + 1;
                    break;
                }
            }
        }
        return importanceQuestion;
    }

    private void translateInterface(DatabaseHelper databaseHelper, SQLiteDatabase db,
                                    int questionNum) {
        switch (questionNum){
            case 1:
                cancel.setText(databaseHelper.getLocalText(db,"Exit"));
                break;
            case 20:
                next.setText(databaseHelper.getLocalText(db,"Finish"));
                break;
            case 12:
                questionText.setTextSize(18);
                break;
            default:
                cancel.setText(databaseHelper.getLocalText(db,"Cancel"));
                next.setText(databaseHelper.getLocalText(db,"Next"));
                questionText.setTextSize(24);
        }
    }

}