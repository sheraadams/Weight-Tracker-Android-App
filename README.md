## Android Weight Tracker App

View the [app video on YouTube](https://youtu.be/9ytK9yUP-Qc)

## Weight Tracker App

![weight1](https://github.com/sheraadams/Weight-Tracker-Android-App/assets/110789514/a35b1779-e8f7-4d8f-aad4-40f5436ea176)

## About the Project
This project is a Java language Android phone weight management program. When a user meets a goal weight (gaining or losing weight), they will receive an SMS notification. This app I added password hashing to protect sensitive user data from unauthorized access. I also added a “remember me” button to allow users to conveniently store login credentials. 

## Database Interaction
This application uses the built-in Android Studio SQLite database. There is a user table and a measurement table and sensitive information is kept separate and is stored in the user table. Passwords are hashed using the SHA-256 algorithm to provide data security. 

```java
    // hashing reference: https://stackoverflow.com/questions/2817752/how-can-i-convert-a-byte-array-to-hexadecimal-in-java
    public String hashPassword(String data){
        MessageDigest md; // Initialize the md object
        try {  // use the SHA-256 algorithm
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {  // catch exception
            e.printStackTrace();
            return "SHA-256 algorithm not found.";
        }
        byte[] hashBytes = md.digest(data.getBytes());  // Generate hash value from the string
        String hashHex = bytesToHex(hashBytes); // Convert hash value to hex
        return hashHex; // return the data
    }

    // Convert byte array to hexadecimal string
    // https://stackoverflow.com/questions/2817752/how-can-i-convert-a-byte-array-to-hexadecimal-in-java
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();  // use string builder to convert to a string
        for (byte b : bytes) {  // iterate through the bytes
            result.append(String.format("%02x", b)); // format the string
        }
        return result.toString();  // return the data as a string
    }
```

## The Singleton Design Pattern
The user class uses the singleton design pattern to help maintain the separation of user data and associate data with the user. Rather than passing user data as an intent (this is not a secure practice), we can create and return an instance of the current user and associate data with the proper user.
```java
package com.example.myweightapp1;

public class User {
    private static User instance;
    private String username;
    private String phone;
    private User() {
        // Private constructor
    }

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    // Getters and setters for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

```
## The Add Goal Method
Goal weight, weight, goal type, phone number, and username are passed as parameters. When a user adds a goal, they can choose whether they wish to gain or add weight. If the user selects gain, (goal type: “gain”), 1 is added to the gain column, and 0 is added to the lose column. This is true also in reverse. 

```java
    // method to add the goal and measurement to the database
    void addGoal(String goalWeight, String weight, String phone, String username) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        ContentValues cv = new ContentValues();  // initialize a cv for insertion into the weights table
        ContentValues cv2 = new ContentValues();   // initialize a cv for insertion into the goal table

        // get the current date and format it
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        cv.put(COLUMN_DATE, currentDate);
        if (!goalWeight.isEmpty()) {  // if the goal weight is not null and not empty
            cv.put(COLUMN_GOAL, goalWeight);  // add it to the respective databases
            cv2.put(COLUMN_GOAL, goalWeight);
        }
        if (weight != null && !weight.isEmpty()) {  // if weight is not null and not empty
            try {
                int weightValue = Integer.parseInt(weight); // parse as int
                cv.put(COLUMN_WEIGHT, weightValue); // add to the db
            } catch (NumberFormatException e) {    // if weight format is not correct
                cv.put(COLUMN_WEIGHT, 0); // add a default value
            }
        } else { // if weight is null or empty
            cv.put(COLUMN_WEIGHT, 0); // add a default value
        }
        if (phone != null && !phone.isEmpty()) {
            cv.put(COLUMN_PHONE, formatPhoneNumber(phone)); // add to the db after formatting
            user.setPhone(phone);
        }
        if (!username.isEmpty()) {  // first check for an empty value
            cv.put(COLUMN_USERNAME, username); // add to the db
            cv.put(COLUMN_TITLE, username); // add to the db
        }

        // check for insertion
        long result = db.insert(WEIGHTS_TABLE, null, cv); // check for insertion
        long result2 = db.insert(GOAL_TABLE, null, cv2); // check for insertion
        if ((result == -1) || (result2 == -1)){  // if either insertion fails
            Toast.makeText(context, "Failed to add goal", Toast.LENGTH_SHORT).show();
        } else {  // in the case of successful insertions
            Toast.makeText(context, "Goal added successfully!", Toast.LENGTH_SHORT).show();
        }
    }
```

## The Add Measurement Method

Title, description, weight, phone number, and username are passed as parameters. When a user adds a measurement, they can add as many or as little details as they wish. Data is always associated with the user to prevent unauthorized access. If there are empty fields, default values are added to the database, otherwise, the user data is added to the database.

```java
       // method to add a measurement to the database
    void addMeasurement(String title, String date, String weight, String username, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        ContentValues cv = new ContentValues();

        // define the current date and format it for a default value
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        cv.put(COLUMN_DATE, currentDate); // add to the db

        if (title == null || title.isEmpty()) {  // if title is null or empty
            cv.put(COLUMN_TITLE, username);
        }// add to the db
        if (title != null && !title.isEmpty()) {  // if title is not null and not empty
            cv.put(COLUMN_TITLE, title); // add to the db
        }
        if (date == null || date.isEmpty()) {  // if date is null or empty
            cv.put(COLUMN_DATE, currentDate); // add a default current date
        }
        if (date != null && !date.isEmpty()) {  // if not null or empty,
            cv.put(COLUMN_DATE, date);  // add the date
        }
        if (weight != null && weight.matches("\\d+")) {
            int weightValue = Integer.parseInt(weight);
            cv.put(COLUMN_WEIGHT, weightValue);
        } else {
            Toast.makeText(context, "Invalid weight input", Toast.LENGTH_SHORT).show();
            return; // exit the method if weight is not a valid integer
        }
        if (phone != null && !phone.isEmpty()) {  // if phone is not null and not empty
            cv.put(COLUMN_PHONE, phone); // add to the db
        } else { // if null or empty
            cv.put(COLUMN_PHONE, ""); // add an empty default value
        }
        if (username != null && !username.isEmpty()) {  // if username is not null and not empty
            cv.put(COLUMN_USERNAME, username);  // add it to the database
        }
        // check for insertion
        long result = db.insert(WEIGHTS_TABLE, null, cv); // check for insertion
        if (result == -1) {  // if the insertion failed, display the message
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else {  // otherwise, success
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
```

## SMS Notifications
If a user chooses to lose weight, they will receive a notification when their weight is less than or equal to the goal. If a user chooses to gain weight, they will receive a notification when their weight is greater than or equal to the goal. If the “Enable SMS” slider is set to on, the Goal Activity calls the setParams(), and startThread() functions of the Notification class to start notifications. The notification class checks if the weight goal is met for the given goal type. If the goal has been met SMS and SMS functionality has been enabled, the SMS message will be sent. Toast messages are used to confirm or deny success.

```java
        public void run() {
            // check for notification condition is met
            if ((gain == 1 && weight >= goalWeight) || (lose == 1 && weight <= goalWeight)) {
                try {
                    // Check Permissions for SEND_SMS
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                            == PackageManager.PERMISSION_GRANTED) {
                        SmsManager smsManager = SmsManager.getDefault();
                        if (phoneNumber != null) {
                            // Send the Message
                            String message = username + " has reached their goal weight: " + goalWeight + ".";
                            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                            Log.d("Notification", "SMS sent successfully!");
                        } else {
                            // case where phone number is not provided
                            Log.d("Notification", "Phone number not provided!");
                        }
                    } else {
                        // case where SEND_SMS permission is not granted
                        Log.d("Notification", "Insufficient SMS permissions!");
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // print exception details to the console
                }
            }
        }
    }
```

## Credential Autofill and Shared Preferences

I added a “remember me” button to allow users to conveniently store login credentials. Using the built-in SharedPreferences Android Studio import, we can store some user preferences for convenience. The saveCredentials() and autofillCredentials() methods remember the username and password when the “Remember Me” button is checked. 

```java
    private void saveCredentials(String username, String password, boolean rememberMe) {
        SharedPreferences preferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // save the credentials in shared preferences
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("rememberMe", rememberMe);

        editor.apply();
    }

    // Retrieve and autofill credentials "Remember Me" was checked
    private void autofillCredentials() {
        SharedPreferences preferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        // default is do not remember the user
        boolean rememberMe = preferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");

            // Autofill the login fields
            username_input.setText(username);
            password_input.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }
```
Using the built-in SharedPreferences Android Studio import, we can store some user preferences for convenience. The saveCredentials() and autofillCredentials() methods remember the username and password when the “Remember Me” button is checked. 

**•    Briefly summarize the requirements and goals of the app you developed. What user needs was this app designed to address?**

Weight tracker is an Android app that is designed to allow users to track their weight goals over time using a user-friendly convenience-focused interface. Weight tracker features a login screen with register and login methods, a goal screen where a weight and goal weight can be entered and the goal of “gain weight”, “lose weight”, or “skip” can be chosen. Users can choose to opt in to SMS text messaging to be notified when they reach their weight goals. When SMS messaging is enabled and a user meets their goal weight, a text message is sent to their device to congratulate them. Weight tracker also ensures a data security as it utilizes password hashing and basic user authentication. 

**•    What screens and features were necessary to support user needs and produce a user-centered UI for the app? How did your UI designs keep users in mind? Why were your designs successful?**

Weight tracker features a login screen, a goal screen, a home screen, and a weight editing screen. Each screen is necessary to support the user’s needs for a simple yet organized app experience. Convenience centered design elements like the “remember me” checkbox on the login page, a “skip” button on the goal page, and auto-filled fields including title (which is the username by default if left blank), and date (the current date) ensure the experience meets the users need for convenience. 

**•    How did you approach the process of coding your app? What techniques or strategies did you use? How could those be applied in the future?**

For this app, I approached the coding slowly and carefully starting with the simplest methods and interfaces and progressing in complexity over time. I always prefer to follow modular development, but this is especially true with larger and more complex projects and with subjects that are new.  Doing so allows us to move forward and develop solutions over time, leaving extra time for the more challenging solutions that often come to us through our work. 

**•    How did you test to ensure your code was functional? Why is this process important and what did it reveal?**

I ensured that my code was functional with white-box testing as I tested my application with the user experience in mind. This process is important as we can identify weaknesses and opportunities for improvement in our code when we thoroughly test it from the user’s perspective. As I tested my code, I found many opportunities for improvement, and I eliminated errors that I would not have been able to identify without thorough testing. 

**•    Considering the full app design and development process, from initial planning to finalization, where did you have to innovate to overcome a challenge?**

As I began testing my code, I became aware that I would need to implement OAuth, Firebase, or a singleton user class to ensure the separation of client data and prevent different users from viewing one another’s data on the main screen.  I created a singleton user class to solve this problem and associate each user with a single instance, encapsulating the user data throughout the application. 

**•    In what specific component from your mobile app were you particularly successful in demonstrating your knowledge, skills, and experience?**

I was successful in demonstrating my knowledge on the home screen in which I created a double column grid on a recycler view with interactive entries that act as buttons, floating action buttons that act as buttons, and a drop-down menu. A lot of work went into the development of this singular screen, and I am proud of how I was able to interweave the knowledge that I gained in this class into the development to meet the requirements of the user. 

<div align="center">
  <p><strong>Proudly crafted with ❤️ by Shera Adams.</strong></p>
</div>

