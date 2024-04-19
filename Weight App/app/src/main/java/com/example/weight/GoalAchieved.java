package com.example.weight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
public class GoalAchieved extends AppCompatActivity {
    // declare the ui elements
    TextView congratulationsMessage;
    FloatingActionButton backToMain;

    // get the username from the singleton class
    User user = User.getInstance();
    String username = user.getUsername();

    SMS notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        // initialize the notification instance
        notification = new SMS(this);

        // initialize the back to main button
        backToMain = findViewById(R.id.backToMainButton);

        // initialize the congratulations message
        congratulationsMessage = findViewById(R.id.congratulations_message);

        // set text colors, sizes and message
        congratulationsMessage.setTextColor(ContextCompat.getColor(this, R.color.purple_200));
        congratulationsMessage.setTextSize(20f); // set size

        // Set the message string
        String message = "Congratulations " + username + "! You have reached your weight goal!";
        congratulationsMessage.setText(message);

        // Once the goal has been met, set the goal to skip
        // doing this will prevent reminders each time the same goal is met in the future
        user.setGoal("skip");

        // Set the listener for the backToMain button
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GoalAchieved.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
