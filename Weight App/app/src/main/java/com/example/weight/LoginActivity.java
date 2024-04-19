package com.example.weight;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    // Declare the ui elements
    EditText username_input;
    EditText password_input;
    Button login_button;
    Button register_button;
    CheckBox remember_me_box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI editTexts
        username_input = findViewById(R.id.username_input);
        password_input = findViewById(R.id.password_input);

        // initialize the buttons
        login_button = findViewById(R.id.login_button);
        register_button = findViewById(R.id.register_button);

        // initialize the checkbox
        remember_me_box = findViewById(R.id.remember_me_box);

        // autofill if "Remember Me" is checked
        autofillCredentials();

        // Register Button Click Listener
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get text from the ui
                String enteredUsername = username_input.getText().toString().trim();
                String enteredPassword = password_input.getText().toString().trim();

                // check if credentials meet requirements
                if (isValidCredentials(enteredUsername, enteredPassword)) {
                    // Save credentials if "Remember Me" checkbox is checked
                    saveCredentials(enteredUsername, enteredPassword, remember_me_box.isChecked());
                    DBHelper myDB = new DBHelper(LoginActivity.this);

                    User user = User.getInstance();  // initialize user singleton instance
                    user.setUsername(enteredUsername);  // save the username singleton

                    myDB.addUser(enteredUsername, enteredPassword);  // save the username to the db
                    myDB.close(); // close the database when done

                    Toast.makeText(LoginActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Login Button Click Listener
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get text from the ui
                String enteredUsername = username_input.getText().toString().trim();
                String enteredPassword = password_input.getText().toString().trim();

                // check if credentials meet requirements
                if (isValidCredentials(enteredUsername, enteredPassword)) {

                    // Save credentials if "Remember Me" is checked
                    saveCredentials(enteredUsername, enteredPassword, remember_me_box.isChecked());
                    User user = User.getInstance();   // initialize user singleton instance
                    user.setUsername(enteredUsername);  // save the username singleton

                    DBHelper myDB = new DBHelper(LoginActivity.this);
                    // if credentials are validated against database, user is logged in
                    boolean isLoggedIn = myDB.checkCredentials(enteredUsername, enteredPassword);
                    myDB.close();

                    if (isLoggedIn) {  // if login is successful, show success and start the main activity
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, GoalActivity.class);
                        startActivity(intent);
                    } else { // if login failed, show error
                        Toast.makeText(LoginActivity.this, "Login failed, please check your credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Save credentials to SharedPreferences on login for user convenience
    private void saveCredentials(String username, String password, boolean rememberMe) {
        // save the credentials in shared preferences
        SharedPreferences preferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // send the credentials to the autofill fields
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("rememberMe", rememberMe);
        editor.apply();
    }

    // Retrieve and autofill credentials if "Remember Me" button was checked
    private void autofillCredentials() {
        SharedPreferences preferences = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        // default is do not remember the user
        boolean rememberMe = preferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            // get the login info from the shared preferences
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");

            // Autofill the login fields
            username_input.setText(username);
            password_input.setText(password);
            remember_me_box.setChecked(true);
        }
    }

    // Validate the entered credentials, setting a minimum length as 6 characters
    private boolean isValidCredentials(String username, String password) {
        if (username.length() >= 6 && password.length() >= 6) {
            return true;
        } else {
            Toast.makeText(LoginActivity.this, "Username and password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
