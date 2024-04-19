package com.example.weight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Intent;
public class SMS {
    // declare the contect and timer variables
    private Context context;
    private Timer timer;

    // Initialize variables
    private int gain = 0;
    private int lose = 0;
    private int weight;
    private int goalWeight;
    private String username;
    private String date;
    private String phoneNumber;

    User user = User.getInstance();
    public SMS(Context context) {
        this.context = context;
    }

    public void setParameters(int gain, int lose, int weight, int goalWeight, String username, String phoneNumber, String date) {
        this.gain = gain;
        this.lose = lose;

        // Check if there is a goal, the variable that is equal to 1 is the goal...
        if (this.gain == 0 && this.lose == 0 ) {  // if there is no goal,
            this.lose = 1;  // set lose as a default goal for now
        }

        this.weight = weight;
        this.goalWeight = goalWeight;
        this.username = user.getUsername();
        this.date = date;
        this.phoneNumber = user.getPhone();
    }

    private class SMSTask extends TimerTask {
        private Context context;
        public SMSTask(Context context) {
            this.context = context;
        }

        public void run() {
            sendSMS(); // check for notification condition is met and send sms if met
        }
    }
    public void sendSMS(){
        DBHelper myDb = new DBHelper(context);  // Initialize myDb
        int checkGoalIsMet = myDb.checkGoal(this.gain, this.lose, this.weight, this.username, this.date);
        if (checkGoalIsMet == 1) {  // if goal is met
            try { // check permissions for sms messages
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    if (this.phoneNumber != null) {  // if permissions granted, and number provided, send the message
                        String message = this.username + " has reached their goal weight!";
                        smsManager.sendTextMessage(this.phoneNumber, null, message, null, null);
                        Intent intent = new Intent(context, GoalAchieved.class);
                        context.startActivity(intent);
                        Log.d("SMS", "SMS sent successfully!");
                    } else {  // if phone number is null
                        Log.d("SMS", "Phone number not provided!");
                    }
                } else {  // sms permission is not granted
                    Log.d("SMS", "Insufficient SMS permissions!");
                }
            } catch (Exception e) {   // if exception
                e.printStackTrace(); // print details to the console
            }
        }
    }
    // check if the notification should be sent
    public void startSMS() {
        if (timer!=null){
            return;  // if there is already a timer, leave it
        } else{ // if there is not a timer, create it
            timer = new Timer(); // create a timer to check every 10 minutes
            timer.scheduleAtFixedRate(new SMSTask(this.context), 0, 60000);
        }
    }
    public void stopSMS() {
        if (timer != null) {   // Cancel the timer if it's not null
            timer.cancel();
            timer.purge();
        }
    }
}
