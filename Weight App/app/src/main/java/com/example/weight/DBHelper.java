package com.example.weight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    User user = User.getInstance();
    private static final String DATABASE_NAME = "weightTracker.db";
    private static final int DATABASE_VERSION = 4;  // increment the database version
    private static final String WEIGHTS_TABLE = "weight_data"; // data tables
    private static final String GOAL_TABLE = "goal_data";
    private static final String USER_TABLE = "user_data";
    private static final String COLUMN_ID = "_id";  // columns
    private static final String COLUMN_TITLE = "measurement_title";
    private static final String COLUMN_DATE = "measurement_date";
    private static final String COLUMN_WEIGHT = "measurement_weight";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_GOAL = "goalWeight";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_USERNAME = "user_username";
    private static final String COLUMN_PASSWORD = "user_password";  // stores a hashed password
    int intWeight;
    int intGoalWeight;
    SMS sms;

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String userTableQuery = "CREATE TABLE " + USER_TABLE +
                " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_PHONE + " TEXT)";

        String weightTableQuery = "CREATE TABLE " + WEIGHTS_TABLE +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_WEIGHT + " INTEGER, " +
                COLUMN_GOAL + " TEXT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PHONE + " TEXT)";

        String goalTableQuery = "CREATE TABLE " + GOAL_TABLE +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_WEIGHT + " INTEGER, " +
                COLUMN_GOAL + " TEXT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PHONE + " TEXT)";

        // load the databases
        db.execSQL(userTableQuery);
        db.execSQL(weightTableQuery);
        db.execSQL(goalTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WEIGHTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GOAL_TABLE);
        onCreate(db);
    }
    // hashing reference:
    // https://stackoverflow.com/questions/2817752/how-can-i-convert-a-byte-array-to-hexadecimal-in-java
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

    // method to add user and hashed password to the database
    void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        ContentValues cv = new ContentValues();
        if (username != null) {  // if the username is not null
            cv.put(COLUMN_USERNAME, username);  // add it to the db
        }
        String hashedPassword = hashPassword(password); // Hash the password
        cv.put(COLUMN_PASSWORD, hashedPassword); // add hashed password to the db
        long result = db.insert(USER_TABLE, null, cv);
        if (result == -1) {  // if the result fails
            Toast.makeText(context, "Failed to add user", Toast.LENGTH_SHORT).show();
        } else {  // otherwise, success
            Toast.makeText(context, "User added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    // method to check user credentials against the database
    boolean checkCredentials(String username, String password) {
        if (username == null || password == null) { // if  username or password is null
            return false; //return false
        }
        SQLiteDatabase db = this.getReadableDatabase();  // initialize the database
        // define the search
        String[] columns = {COLUMN_USER_ID, COLUMN_PASSWORD};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(USER_TABLE, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {   // User with the provided username
            int passwordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            if (passwordColumnIndex != -1) {  // if the password column is found
                // Retrieve the stored hashed password from the db
                String storedPasswordHash = cursor.getString(passwordColumnIndex);
                String hashedPassword = hashPassword(password); // Hash the password
                cursor.close();
                // Check if the hashed password matches the stored hashed password
                return hashedPassword.equals(storedPasswordHash);
            }
        }
        cursor.close();
        return false; //  return false if the provided username doesn't exist or columns not found.
    }

    void addPhone(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        ContentValues cv = new ContentValues();
        if (phone != null) {  // check for null phone
            cv.put(COLUMN_PHONE, formatPhoneNumber(phone));  // add the formatted phone number to the db
        }
        // check for insertion
        long result = db.insert(USER_TABLE, null, cv); // check for insertion
        if (result == -1) {  // if the insertion fails
            Toast.makeText(context, "Failed to add phone number", Toast.LENGTH_SHORT).show();
        } else {  // otherwise, success
            Toast.makeText(context, "Phone number added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to format phone number
    public String formatPhoneNumber(String rawPhoneNumber) {
        String formattedPhoneNumber = rawPhoneNumber.replaceAll("[^0-9]", "");
        return formattedPhoneNumber;
    }

    // method to read data
    Cursor readAllData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();  // initialize the database
        String query = "SELECT * FROM " + WEIGHTS_TABLE + // read all non-null data for the selected user
                " WHERE " + COLUMN_USERNAME + " = ?" +
                " AND " + COLUMN_DATE + " IS NOT NULL " +
                " AND " + COLUMN_WEIGHT + " IS NOT NULL";
        Cursor cursor = null;
        try {
            if (db != null) {  // if the database is not null
                String[] selectionArgs = {username}; // define the arguments
                cursor = db.rawQuery(query, selectionArgs);  // search the query
            }
        } catch (Exception e) {  // catch exceptions
            e.printStackTrace();
        }
        return cursor;  // return the data in a cursor
    }

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
    public void getAndCheckGoal(String selectedDate, String weight, String username, Context context){
        sms = new SMS(context);
        // get the goal weight from the  database
        String goalWeightStr = getMostCurrentGoalWeight(username);

        // determine the most recent weight goal, the value equal to 1 is the goal
        String goal = user.getGoal();
        int gain = (goal.equals("gain")) ? 1 : 0; // if the goal is "gain", gain = 1, otherwise 0
        int lose = (goal.equals("lose")) ? 1 : 0; // if the goal is "lose", lose = 1, otherwise 0
        int skip = (goal.equals("skip")) ? 1 : 0; // if the goal is "skip", skip = 1, otherwise 0
        try {
            if (weight != null) {
                intWeight = Integer.parseInt(weight);
            }
            if (goalWeightStr != null) {
                intGoalWeight = Integer.parseInt(goalWeightStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        // start the notification thread if the user has a goal
        if (skip!= 1){
            sms.setParameters(gain, lose, intWeight, intGoalWeight, username, user.getPhone(), selectedDate);
            sms.startSMS();
        }
    }

    // Method to update an entry in the db
    void updateData(String row_id, String title, String date, String weight, String username) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the db
        ContentValues cv = new ContentValues();
        if (!title.isEmpty()) {  // if the username is not empty
            cv.put(COLUMN_TITLE, title); // add to the db
        }
        if ((!username.isEmpty() || username !=null )&& title.isEmpty()) {  // if the username is not empty
            cv.put(COLUMN_TITLE, username); // add to the db
        }
        if (!date.isEmpty()) {  // if the date is not empty
            cv.put(COLUMN_DATE, date); // add to the db
        }
        if (weight != null && !weight.isEmpty()) {  // if weight is not null and not empty
            try {
                int weightValue = Integer.parseInt(weight); // parse as int
                cv.put(COLUMN_WEIGHT, weightValue); // add to the db
            } catch (NumberFormatException e) {    // catch non-valid formats
                cv.put(COLUMN_WEIGHT, 0); // add a default value
            }
        } else { // if null or empty
            cv.put(COLUMN_WEIGHT, 0); // add a default value
        }
        // check for update
        long result = db.update(WEIGHTS_TABLE, cv, "_id=?", new String[]{row_id}); // check for insertion
        if (result == -1) {  // if insertion fails
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else {  // otherwise, success
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    // after a weight has been added, check if the user's goal has been met recently
    int checkGoal(int gain, int lose, int weight, String username, String date) {
        // if gain is 1, the goal is gain, if lose is 1 the goal is lose,
        // if neither, the goal will be set to default lose = 1
        try {
            SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
            if (db == null) {  // check for a null database
                Log.e("Database", "Unable to open or create the database");
                return 0;
            }

            // first check that the measurement is new, if it is old, a goal was not recently met
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());  // define the current date
            if (date.compareTo(currentDate) < 0)  {  // If the date is current, proceed
                return 0;
            }

            // if the measurement is current, proceed to check the weight against the goal
            // define the query
            String query = "SELECT * FROM " + WEIGHTS_TABLE +
                    " WHERE " + COLUMN_USERNAME + " = ?" +  // check the username
                    " AND " + COLUMN_DATE + " IS NOT NULL" +   // check for nulls
                    " AND " + COLUMN_WEIGHT + " IS NOT NULL" +
                    " AND " + COLUMN_GOAL + " IS NOT NULL";
            Cursor cursor = db.rawQuery(query, new String[]{username});
            if (cursor != null && cursor.moveToFirst()) {  // iterate through the data if it exists
                int columnIndexGoal = cursor.getColumnIndex(COLUMN_GOAL);  // get the index
                int storedGoal = cursor.getInt(columnIndexGoal); // get the goal weight
                cursor.close();
                if (lose == 1 && weight <= storedGoal) { // if the goal is lose and the weight is less than or equal to the goal
                    return 1; // goal met for losing weight
                }
                if (gain == 1 && weight >= storedGoal) { // if the goal is gain and the weight is greater than or equal to the goal
                    return 1; // goal met for gaining weight
                }
            }
            return 0; // no goal met
        } catch (Exception e) {  // check for exceptions
            System.out.println("Exception in checkGoal method: " + e.getMessage());
            return 0;
        }
    }

    public String getMostCurrentGoalWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();  // initialize the database
        String result = null; // initialize the result and set it to null
        if (db != null) {  // if the database is not null
            String query = "SELECT " + COLUMN_GOAL + " FROM " + WEIGHTS_TABLE + " WHERE "
                    + COLUMN_USERNAME + " = ?" + " AND " + COLUMN_GOAL + " IS NOT NULL" +
                    " ORDER BY " + COLUMN_DATE + " DESC LIMIT 1";  // most recent goal filter out nulls
            Cursor cursor = db.rawQuery(query, new String[]{username});
            if (cursor != null && cursor.moveToFirst()) {  // check for null cursor
                int columnIndexGoal = cursor.getColumnIndex(COLUMN_GOAL);
                result = cursor.getString(columnIndexGoal);
                cursor.close();  // close the cursor
            }
        }
        return result;
    }
    // method to delete an entry
    void deleteWeight(String row_id) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        long result = db.delete(WEIGHTS_TABLE, "_id=?", new String[]{row_id});
        if (result == -1) {  // if the result fails
            Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
        } else {  // otherwise, success
            Toast.makeText(context, "Successfully Deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    // method to clear all entries
    void deleteAllWeights(String username) {
        SQLiteDatabase db = this.getWritableDatabase();  // initialize the database
        if (db != null) {  // if the database is not null, delete all data
            db.delete(WEIGHTS_TABLE, COLUMN_USERNAME + "=?", new String[]{username});
        }
    }
}