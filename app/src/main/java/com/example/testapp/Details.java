package com.example.testapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class Details extends AppCompatActivity {
    String details;
    String recipeName;
    int stepNum;
    private long startTime;    //in ms
    private long endTime;
    private TextView timerTextView;
    private EditText inputTime;
    private Button setTimeButton;
    private Button startPauseTimer;
    private Button resetTimer;
    private CountDownTimer countDownTimer;
    private  boolean timerRunning;
    private long timeLeft = startTime;  //in ms
    private Button completedButton;
    private int stepsDone = 0;
//    String status;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

//        status = "In Progress";

        // Get information from previous screen
        details = getIntent().getExtras().getString("details");
        recipeName = getIntent().getExtras().getString("name");
        stepNum = getIntent().getExtras().getInt("position");

        // Get textview instances
        TextView detailsTextView = (TextView) findViewById(R.id.textView);
        TextView nameTextView = findViewById(R.id.fooditem);
        TextView stepNumTextView = findViewById(R.id.step_num);
        completedButton = findViewById(R.id.completed);

        // Set textviews
        detailsTextView.setText(details);
        Log.e("the details", details);
        nameTextView.setText(recipeName);
        stepNumTextView.setText("Step: " + Integer.toString(stepNum));
        completedButton = findViewById(R.id.completed);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);      // Disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // Remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // Remove the icon
        }

        // Get timer elements
        timerTextView = findViewById(R.id.textView_countdown);
        startPauseTimer = findViewById(R.id.startTimer);
        resetTimer = findViewById(R.id.resetTimer);
        inputTime = findViewById(R.id.inputTime);
        setTimeButton = findViewById(R.id.setButton);

        // Get the time inputted by user
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = inputTime.getText().toString();
                if(input.length() == 0){
                    Toast.makeText(Details.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input) * 60000; //convert minutes to millis
                if(millisInput == 0){
                    Toast.makeText(Details.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                inputTime.setText("");
            }
        });

        // Call timer with user inputted time
        timerFunctionality();

        completedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                status = "Completed";
//                Intent intent = new Intent(Details.this, Chicken.class);
//                intent.putExtra("status", status);
//                intent.putExtra("class", "Details");
//                intent.putExtra("stepNum", stepNum);
//                setResult(RESULT_OK, intent);

                finish();
                return;
            }
        });
    }

    // Close keyboard
    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Countdown timer
    public void timerFunctionality(){
        startPauseTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerRunning){
                    pauseTimer();
                }
                else{
                    startTimer();
                }
            }
        });

        resetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimerFunc();
            }
        });

        updateTimerText();
    }

    // Methods for the countdown timer
    private void setTime(long milliseconds){
        startTime = milliseconds;
        resetTimerFunc();
        closeKeyboard();
    }

    private void startTimer(){
        endTime = System.currentTimeMillis() + timeLeft;

        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                startPauseTimer.setText("Start Timer");
//                startPauseTimer.setVisibility(View.INVISIBLE);
//                resetTimer.setVisibility(View.VISIBLE);
            }
        }.start();

        timerRunning = true;
        startPauseTimer.setText("Pause Timer");
//        resetTimer.setVisibility(View.INVISIBLE);
        setTimeButton.setEnabled(false);
    }

    private void pauseTimer(){
        countDownTimer.cancel();
        timerRunning = false;
        startPauseTimer.setText("Start Timer");
//        resetTimer.setVisibility(View.VISIBLE);
        setTimeButton.setEnabled(false);
    }

    private void resetTimerFunc(){
        timeLeft = startTime;
        updateTimerText();
//        resetTimer.setVisibility(View.INVISIBLE);
//        startPauseTimer.setVisibility(View.VISIBLE);
        setTimeButton.setEnabled(true);
    }

    // Convert remaining time into minutes and seconds
    private void updateTimerText(){
        int hours = (int) (timeLeft / 1000) / 3600;
        int minutes = (int)((timeLeft/1000) % 3600) / 60;
        int seconds = (int)(timeLeft / 1000) % 60;

        String timeLeftFormatted;

        // Check if time is longer than 0 hours
        if(hours > 0){
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        }
        else{
            timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        }
        timerTextView.setText(timeLeftFormatted);
    }

//    private void updateButtons() {
//        if (timerRunning) {
//            startPauseTimer.setText("Pause Timer");
//        } else {
//            startPauseTimer.setText("Start Timer");
//        }
//    }
    // Preserve state of timer
    /*
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("millisLeft", timeLeft);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", endTime);
        editor.apply();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        timeLeft = prefs.getLong("millisLeft", startTime);
        timerRunning = prefs.getBoolean("timerRunning", false);
        updateTimerText();
        updateButtons();
        if (timerRunning) {
            endTime = prefs.getLong("endTime", 0);
            timeLeft = endTime - System.currentTimeMillis();
            if (timeLeft < 0) {
                timeLeft = 0;
                timerRunning = false;
                updateTimerText();
                updateButtons();
            } else {
                startTimer();
            }
        }
    }

    */
}