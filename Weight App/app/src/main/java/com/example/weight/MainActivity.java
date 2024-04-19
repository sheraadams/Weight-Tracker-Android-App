package com.example.weight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private FloatingActionButton add_button;

    // Declare the Lists
    private ArrayList<String> measurement_id;
    private ArrayList<String> measurement_title;
    private ArrayList<String> measurement_date;
    private ArrayList<String> measurement_weight;

    // declare the recycler view adapter
    private RecyclerView customAdapter;
    // declare the SMS switch and switch state boolean
    private boolean smsSwitchState;
    // get the username from the singleton class
    private User user = User.getInstance();
    private String username = user.getUsername();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper myDB = new DBHelper(MainActivity.this);
        // load data to the ui

        // Initialize GUI components
        recyclerView = findViewById(R.id.recyclerView);
        add_button = findViewById(R.id.add_button);

        // Create array lists to store values
        measurement_id = new ArrayList<>();
        measurement_title = new ArrayList<>();
        measurement_date = new ArrayList<>();
        measurement_weight = new ArrayList<>();
        // store data in arrays
        Cursor cursor = myDB.readAllData(username);
        if (cursor != null) {
            if (cursor.getCount() > 0) {  // if there is data
                while (cursor.moveToNext()) {  // get the data
                    measurement_id.add(cursor.getString(0));
                    measurement_title.add(cursor.getString(1));
                    measurement_date.add(cursor.getString(2));
                    measurement_weight.add(cursor.getString(3));
                }
            }
            cursor.close(); // close the cursor
        }
        myDB.close();

        // Add arrays to the recycler view
        customAdapter = new RecyclerView(MainActivity.this, this, measurement_id, measurement_title, measurement_date, measurement_weight);
        recyclerView.setAdapter(customAdapter);

        // set the number of rows in the grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // handle sms preferences
        // Retrieve SMS preferences from the goal activity intent
        Intent intent = getIntent();
        if (intent.hasExtra("sms_switch_state")) {
            smsSwitchState = intent.getBooleanExtra("sms_switch_state", false);
        }

        // assign a shared preference for the boolean value of the sms switch
        SharedPreferences sharedPreferences = getSharedPreferences("my_settings", MODE_PRIVATE);
        smsSwitchState = sharedPreferences.getBoolean("sms_switch_state", false);


        // Set OnClickListener function for the add button
        add_button.setOnClickListener(view -> {
            Intent addIntent = new Intent(MainActivity.this, AddActivity.class);
            addIntent.putExtra("sms_switch_state", smsSwitchState);
            startActivity(addIntent);
        });
    }

    // Refresh the UI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            recreate();
        }
    }

    // Menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // Menu option to delete all records
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.menu_logout) {  // logout menu
            confirmLogoutDialog();
            return true;
        }else if (item.getItemId()== R.id.delete_all) {
            confirmDeleteMessage();
            return true;
        }else if (item.getItemId()== R.id.launch_goal) {
            launchGoal();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Confirm delete dialog
    private void confirmDeleteMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // dialog 1
        builder.setTitle("Clear All?");
        // dialog 2
        builder.setMessage("Are you sure you want to clear all data?");  // confirm before deletion

        // if user selects yes
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            DBHelper myDB = new DBHelper(MainActivity.this);
            myDB.deleteAllWeights(username);  // delete all user data
            myDB.close(); // close the db when finished

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent); // Restart the activity
            finish();
        });

        // if user selects no
        builder.setNegativeButton("No", (dialogInterface, i) -> {}); // do nothing if the user cancels
        builder.create().show();
    }

    private void confirmLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // dialog 1
        builder.setTitle("Log Out?");
        // dialog 2
        builder.setMessage("Are you sure you want to log out?");

        // if yes dialog
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            // edit the shared preferences
            SharedPreferences preferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // set "Remember Me" to false
            editor.putBoolean("rememberMe", false);
            editor.apply();

            // start the login activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);  // restart
            startActivity(intent);
            finish();
        });

        // if no dialog
        builder.setNegativeButton("No", (dialogInterface, i) -> {}); // cancel if no
        builder.create().show();
    }
    private void launchGoal(){
    // start the goal activity
    Intent intent = new Intent(MainActivity.this, GoalActivity.class);
    startActivity(intent);
    }
}
