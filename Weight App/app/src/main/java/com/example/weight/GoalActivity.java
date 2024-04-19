package com.example.weight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class GoalActivity extends AppCompatActivity {

    // Declare the editTexts
    EditText goalWeightInput;
    EditText phoneInput;
    EditText weightInput;

    // Declare the buttons
    Button loseButton;
    Button gainButton;
    Button skipButton;

    // Get user instance
    User user = User.getInstance();
    String username = user.getUsername();

    // initialize string variables
    String phoneNumber = "";
    String userWeightStr = "";
    String goalStr = "";

    // declare the sms switch

    private SwitchCompat enableSMSSwitch;

    // define the request code
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;

    SMS sms;
    final DBHelper myDB = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        // Initialize the notification instance
        sms = new SMS(this);

        // get the edit text variables from the ui
        weightInput = findViewById(R.id.weight_input);
        phoneInput = findViewById(R.id.phone_input);
        goalWeightInput = findViewById(R.id.goal_input);

        // get the button from the ui
        loseButton = findViewById(R.id.lose_button);
        gainButton = findViewById(R.id.gain_button);
        skipButton = findViewById(R.id.skip_button);

        // Set a listeners for the lose button, set the goal type to lose
        loseButton.setOnClickListener(view -> handleButtonClick("lose", myDB));

        // Set a listener for the gain button, and set the goal type to gain
        gainButton.setOnClickListener(view -> handleButtonClick("gain", myDB));

        // Set a listener for the skip button
        skipButton.setOnClickListener(view -> {
            Intent intent = new Intent(GoalActivity.this, MainActivity.class);
            startActivity(intent);
            sms.stopSMS();
        });

        handleSMS();
    }

    private void handleSMS(){
        // Check if the permission is granted in the manifest
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission from the user device
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
        // SMS Preferences shared across activities
        SharedPreferences sharedPref = getSharedPreferences("my_settings", Context.MODE_PRIVATE);

        // assign a shared preference to store value of the sms switch
        boolean switchState = sharedPref.getBoolean("sms_switch_state", false);

        // Initialize the enable SMS switch button from the ui
        enableSMSSwitch = findViewById(R.id.enableSMS);
        enableSMSSwitch.setChecked(switchState);

        // if the switch is checked on
        enableSMSSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPref.edit();

            // save the sms state in shared preferences
            editor.putBoolean("sms_switch_state", isChecked);
            editor.apply();

            if (isChecked) {  // Enable SMS if checked
                Toast.makeText(this, "SMS functionality enabled", Toast.LENGTH_SHORT).show();
            } else { // Disable SMS if not checked
                Toast.makeText(this, "SMS functionality disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // request sms permission from the user
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) { // if permissions request sent
            // if permissions are granted, send sms
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show();
            } else { // permission denied, send toast
                Log.d("SMS", "SMS permission denied!");
                Toast.makeText(this, "SMS permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleButtonClick(String goalType, DBHelper myDB) {
        // Set the user variables from the ui inputs
        userWeightStr = weightInput.getText().toString().trim();
        goalStr = goalWeightInput.getText().toString().trim();
        phoneNumber = phoneInput.getText().toString().trim();

        if (!goalStr.isEmpty() && !userWeightStr.isEmpty()) { // if there is data
            try {  // if not empty
                if (phoneNumber != null && !phoneNumber.isEmpty()) {  // Add phone number to the database
                    myDB.addPhone(phoneNumber);
                    user.setPhone(phoneNumber);
                }
                user.setGoal(goalType);  // define the goal type

                // Add the data to the database, closing it when finished before starting a new activity
                myDB.addGoal(goalStr, userWeightStr, user.getPhone(), username);
                myDB.close();

                // Start the main activity with intent
                Intent intent = new Intent(GoalActivity.this, MainActivity.class);
                intent.putExtra("sms_switch_state", enableSMSSwitch.isChecked());
                startActivity(intent);

            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(GoalActivity.this, "Invalid number format for weight or goal.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(GoalActivity.this, "Please enter a valid goal weight.", Toast.LENGTH_SHORT).show();
        }
        myDB.close();
    }
}
