package com.example.weight;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.DatePicker;
public class EditActivity extends AppCompatActivity {

    // declare the ui elements
    EditText title_input;
    EditText weight_input;
    DatePicker updateDatePicker;
    Button update_button;
    Button delete_button;


    // call the user singleton class and define the user
    User user = User.getInstance();
    String username = user.getUsername();

    // declare variables
    String id;
    String title;
    String weight;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // set the action bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            actionBar.setTitle("Edit Weights");
        }

        // initialize the ui components
        title_input = findViewById(R.id.title_input2);
        weight_input = findViewById(R.id.weight_input2);
        update_button = findViewById(R.id.update_button);
        delete_button = findViewById(R.id.delete_button);
        updateDatePicker = findViewById(R.id.update_datePicker);

        // get info from the intent and set the ui texts
        if(getIntent().hasExtra("id") && getIntent().hasExtra("title") &&
                getIntent().hasExtra("date") && getIntent().hasExtra("weight")){

            //Get Intent data
            id = getIntent().getStringExtra("id");
            title = getIntent().getStringExtra("title");
            selectedDate = getIntent().getStringExtra("date");
            weight = getIntent().getStringExtra("weight");

            // Set the ui text variables and send to the ui
            title_input.setText(title);
            weight_input.setText(weight);
        } else{
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        }

        // calendar
        Calendar defaultDate = Calendar.getInstance();
        updateDatePicker.init(defaultDate.get(Calendar.YEAR),
                defaultDate.get(Calendar.MONTH),
                defaultDate.get(Calendar.DAY_OF_MONTH),
                null
        );

        // Add a listener to the calendar to set the date
        updateDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                selectedDate = formatDate(year, month, day);  // format the date
            }
        });

        // add a listener for the delete button
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });

        // add a listener for the update button
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper myDB = new DBHelper(EditActivity.this);  // initialize the db object

                // get the text from the ui and cast to string, removing whitespace
                title = title_input.getText().toString().trim();
                weight = weight_input.getText().toString().trim();

                // update the data passing the user's data as parameters
                myDB.updateData(id, title, selectedDate, weight, username);

                // check if the new weight meets the goal
                myDB.getAndCheckGoal(selectedDate, weight, username, EditActivity.this);
                myDB.close(); // close the db when finished

                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent); // Restart the activity
            }
        });
    }

    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    // Method to confirm before deletion
    void confirmDelete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // dialog 1
        builder.setTitle("Delete " + title + " ?");
        // dialog 2
        builder.setMessage("Are you sure you want to delete " + title + " ?");

        // if the user selects yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DBHelper myDB = new DBHelper(EditActivity.this);
                myDB.deleteWeight(id);
                myDB.close();
                finish();
            }
        });

        // if the user selects no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
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
        if (item.getItemId() == R.id.delete_all) {
            confirmDeleteMessage();
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

        // if the user selects yes
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            DBHelper myDB = new DBHelper(EditActivity.this);
            myDB.deleteAllWeights(username);  // delete all user data

            myDB.close();

            Intent intent = new Intent(EditActivity.this, EditActivity.class);
            startActivity(intent); // Restart the activity
            finish();
        });

        // if the user selects no
        builder.setNegativeButton("No", (dialogInterface, i) -> {}); // do nothing if the user cancels
        builder.create().show();
    }
}