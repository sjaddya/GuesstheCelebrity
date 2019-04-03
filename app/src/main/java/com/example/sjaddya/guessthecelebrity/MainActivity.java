package com.example.sjaddya.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb, locationOfCorrectAnswer, locationOfIncorrectAnswers;
    String[] answers = new String[4];

    ImageView imageView;
    Button button0, button1, button2, button3;

    public void DispToast(String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    public void celebChosen(View view) throws ExecutionException, InterruptedException {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            DispToast("Correct!");
        } else {
            DispToast("Incorrect! It is " + celebNames.get(chosenCeleb));
        }

        newQuestion();
    }

    public class CodeDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String code = "";

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while(data!=-1) {
                    char curr = (char) data;
                    code += curr;
                    data = inputStreamReader.read();
                }

                return code;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Failed";
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void newQuestion() throws ExecutionException, InterruptedException {

        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLs.size());

        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap celebImage;

        celebImage = imageDownloader.execute(celebURLs.get(chosenCeleb)).get();
        imageView.setImageBitmap(celebImage);

        locationOfCorrectAnswer = random.nextInt(4);
        for(int i=0; i<4; i++) {

            if(i == locationOfCorrectAnswer) {

                answers[i] = celebNames.get(chosenCeleb);

            } else {

                locationOfIncorrectAnswers = random.nextInt(celebURLs.size());

                while(locationOfIncorrectAnswers == chosenCeleb) {

                    locationOfIncorrectAnswers = random.nextInt(celebURLs.size());

                }

                answers[i] = celebNames.get(locationOfIncorrectAnswers);

            }
        }

        button0.setText(answers[0]);
        button1.setText(answers[1]);
        button2.setText(answers[2]);
        button3.setText(answers[3]);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.celebImageVIew);
        button0 = findViewById(R.id.optionA);
        button1 = findViewById(R.id.optionB);
        button2 = findViewById(R.id.optionC);
        button3 = findViewById(R.id.optionD);

        CodeDownloader codeDownloader = new CodeDownloader();
        String s = null;

        try {
            s = codeDownloader.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult = s.split("<div class=\"sidebarContainer\">");
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()){
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while(m.find()){
                celebNames.add(m.group(1));
            }

            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
