/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.SnapShare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    EditText txtUser;
    EditText txtPassword;
    TextView viewChangeSignUpMode;
    Boolean signUpModeActive = false;

    //Jump to UserListActivity
    public void showUserList() {

        Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
        startActivity(intent);
    }


    //This method will be called everytime when a key is pressed in the txtPassword
    @Override
    public boolean onKey(View view, int i, KeyEvent event) {

        //We want to set the event on the "enter" key
        if (i == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

            signUp(view);

        }

        return false;

    }

    //This method will be called when there's a click on specific location
    @Override
    public void onClick(View view) {

        //Switch SignUp/LogIn Mode
        if (view.getId() == R.id.viewChangeSignUpMode) {

            Button btnSignUp = (Button) findViewById(R.id.btnSignUp);

            if (signUpModeActive) {

                signUpModeActive = false;
                btnSignUp.setText("Log in");
                viewChangeSignUpMode.setText("New User? Sign Up HERE!");

            } else {

                signUpModeActive = true;
                btnSignUp.setText("Sign up");
                viewChangeSignUpMode.setText("Already has an account? Log in HERE!");

            }

        //Hide the keyboard when user taps outside of the keyboard
        } else if (view.getId() == R.id.backgroundRelativeLayout || view.getId() == R.id.viewLogo) {

            //Gets the keyboard for us
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            //Shut down the keyboard
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }

    }

    //Toast Message
    public void toastMessage(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    //SignUp/LogIn button
    public void signUp(View view) {

        txtUser = (EditText) findViewById(R.id.txtUser);

        if (txtUser.getText().toString().matches("") || txtPassword.getText().toString().matches("")) {

            toastMessage("A username and password are required.");

        } else {

            //If in signUpModeActive sign up a new user
            if (signUpModeActive) {

                ParseUser user = new ParseUser();

                user.setUsername(txtUser.getText().toString().toLowerCase());
                user.setPassword(txtPassword.getText().toString());

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) {

                            toastMessage("New user created! \nYour username is: " + txtUser.getText().toString() + "! ");

                            showUserList();

                        } else {

                            //This is to get error message from Parse
                            toastMessage(e.getMessage());
                            Log.i("Creation failed", e.getMessage());
                        }
                    }
                });

                //if not in signUpModeActive, log in the user
            } else {

                ParseUser.logInInBackground(txtUser.getText().toString().toLowerCase(), txtPassword.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {

                        if (user != null) {

                            toastMessage("You're logged in!");
                            showUserList();

                        } else {

                            toastMessage(e.getMessage());

                        }
                    }
                });
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewChangeSignUpMode = (TextView) findViewById(R.id.viewChangeSignUpMode);
        viewChangeSignUpMode.setOnClickListener(this);

        RelativeLayout backgroundRelativeLayout = (RelativeLayout) findViewById(R.id.backgroundRelativeLayout);
        backgroundRelativeLayout.setOnClickListener(this);

        ImageView viewLogo = (ImageView) findViewById(R.id.viewLogo);
        viewLogo.setOnClickListener(this);

        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setOnKeyListener(this);

        //Verify if user is already logged in. If yes, show the UserList
        if(ParseUser.getCurrentUser() != null) {

            showUserList();

        }

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }


}