package com.example.testyourraceintes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void  StartTestOnClick(View v) {
        Intent intent = new Intent(this, QuestionsActivity.class);
        startActivity(intent);
    }

    public void ShowResultOnClick(View v) {
        Intent intent = new Intent(this,ResultTestActivity.class);
        startActivity(intent);
    }
    public void AboutAuthor(View v) {

        try {
            String content = readFile();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("About Author").setMessage(content).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFile() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean kirillitcaHave = false;
        for (RuLanguages r : RuLanguages.values()){
            if(Locale.getDefault().getLanguage().equals(r.toString())){
                kirillitcaHave = true;
                break;
            }
        }
        if(kirillitcaHave){
            InputStream inputStream = this.getResources().openRawResource(R.raw.about_aputor_ru);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        }
        else {
            InputStream inputStream = this.getResources().openRawResource(R.raw.about_aputor_en);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        }
    }
}