package com.example.weight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {

    // declare edit texts
    EditText title_input;
    EditText weight_input;

    // declare buttons
    Button add_button;

    // declare date picker
    DatePicker datePicker;

    // declare variables
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // initialize the user instance
        User user = User.getInstance();
        String username = user.getUsername();
        String phone = user.getPhone();

        // set the variables to the contents of the EditText inputs from the UI
        title_input = findViewById(R.id.title_input);
        weight_input = findViewById(R.id.weight_input);
        add_button = findViewById(R.id.add_button);
        datePicker = findViewById(R.id.datePicker);

        // add a listener to the date selector
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                // format the selected date
                selectedDate = year + "-" + (month + 1) + "-" + day;
                Toast.makeText(AddActivity.this, "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
            }
        });
        // add_button listener
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the value from the ui
                String weight = weight_input.getText().toString().trim();
                String title = title_input.getText().toString().trim();

                // open the database
                DBHelper myDB = new DBHelper(AddActivity.this);

                // add the measurement to the database
                myDB.addMeasurement(title, selectedDate, weight, username, phone);

                // check if the new weight meets the goal
                myDB.getAndCheckGoal(selectedDate, weight, username, AddActivity.this);
                myDB.close();  // close the database when finished

                // go back to the main activity with intent
                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
