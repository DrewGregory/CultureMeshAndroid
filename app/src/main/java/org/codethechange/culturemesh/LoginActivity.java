package org.codethechange.culturemesh;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.codethechange.culturemesh.models.User;


public class LoginActivity extends RedirectableAppCompatActivity {
    private boolean signInToggle = true;
    EditText firstNameText;
    EditText lastNameText;
    EditText confirmPassword;
    EditText passwordText;
    TextView needAccountText;
    private RequestQueue queue;

    /**
     * Largely for testing, this public method can be used to set which user is currently logged in
     * This is useful for PickOnboardingStatusActivity because different login states correspond
     * to different users. No logged-in user is signalled by a missing SharedPreferences entry.
     * @param settings The SharedPreferences storing user login state
     * @param userID ID of the user to make logged-in
     */
    public static void setLoggedIn(SharedPreferences settings, long userID, String email, String password) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(API.CURRENT_USER, userID);
        editor.putString(API.USER_EMAIL, email);
        editor.putString(API.USER_PASS, password);
        editor.apply();
    }

    public static boolean isLoggedIn(SharedPreferences settings) {
        return settings.contains(API.CURRENT_USER);
    }

    public static void setLoggedOut(SharedPreferences settings) {
        if (isLoggedIn(settings)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(API.CURRENT_USER);
            editor.remove(API.USER_PASS);
            editor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Button signInButton = findViewById(R.id.sign_in_button);
        queue = Volley.newRequestQueue(getApplicationContext());
        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText emailField = findViewById(R.id.user_name_field);
                EditText passwordField = findViewById(R.id.password_field);
                final String email = emailField.getText().toString();
                final String password = passwordField.getText().toString();

                API.Get.userID(queue, email, new Response.Listener<NetworkResponse<Long>>() {
                    @Override
                    public void onResponse(NetworkResponse<Long> response) {
                        if (response.fail()) {
                            response.showErrorDialog(LoginActivity.this);
                        } else {
                            final long id = response.getPayload();
                            API.Get.loginTokenWithCred(queue, email, password, new Response.Listener<NetworkResponse<String>>() {
                                @Override
                                public void onResponse(NetworkResponse<String> response) {
                                    if (response.fail()) {
                                        response.showErrorDialog(LoginActivity.this);
                                    } else {
                                        SharedPreferences settings = getSharedPreferences(API.SETTINGS_IDENTIFIER, MODE_PRIVATE);
                                        setLoggedIn(settings, id, email, password);

                                        Intent returnIntent = new Intent();
                                        setResult(Activity.RESULT_OK, returnIntent);
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        firstNameText = findViewById(R.id.first_name_field);
        lastNameText = findViewById(R.id.last_name_field);
        confirmPassword = findViewById(R.id.confirm_password_field);
        passwordText = findViewById(R.id.password_field);
        needAccountText = findViewById(R.id.need_account_text);
        final Button signToggleButton = findViewById(R.id.sign_toggle_button);
        //Get number of pixels for 8dp
        DisplayMetrics displaymetrics = new DisplayMetrics();
        final int eightDp = (int) getResources().getDimensionPixelSize(R.dimen.edit_text_spacing);
        signToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signInToggle) {
                    //Have animation move edit texts in place.
                    //Move first name text from bottom to just under username
                    Animation firstNameTextAnim = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                                    firstNameText.getLayoutParams();
                            params.topMargin = (int)(1000 - (1000 - eightDp) * interpolatedTime);
                            firstNameText.setLayoutParams(params);
                        }
                    };
                    firstNameTextAnim.setDuration(300); // in ms
                    firstNameText.startAnimation(firstNameTextAnim);
                    //Have Password move two EditText's below where it is now.
                    final Animation passwordTextAnim = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                                    passwordText.getLayoutParams();
                            params.topMargin = (int) (eightDp + interpolatedTime * (
                                    firstNameText.getMeasuredHeight() + eightDp +
                                            lastNameText.getMeasuredHeight()+ eightDp));
                            passwordText.setLayoutParams(params);
                        }
                    };
                    passwordTextAnim.setDuration(300);
                    passwordText.startAnimation(passwordTextAnim);
                    //Have confirmPasswordMove just below password.
                    final Animation confirmPasswordAnim = new Animation(){
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                                    confirmPassword.getLayoutParams();
                            params.topMargin = (int) (eightDp + interpolatedTime * (
                                    firstNameText.getMeasuredHeight() + eightDp +
                                            lastNameText.getMeasuredHeight() + eightDp +
                                            passwordText.getMeasuredHeight() + eightDp));
                            confirmPassword.setLayoutParams(params);
                        }
                    };
                    confirmPasswordAnim.setDuration(300);
                    confirmPassword.startAnimation(confirmPasswordAnim);
                    ConstraintSet constraints = new ConstraintSet();
                    ConstraintLayout layout = findViewById(R.id.login_layout);
                    constraints.clone(layout);
                    //Have sign up button be under confirm password field
                    constraints.connect(R.id.sign_in_button, ConstraintSet.TOP,
                            R.id.confirm_password_field, ConstraintSet.BOTTOM);
                    constraints.applyTo(layout);
                    signInToggle = false;
                    //Swap button labels.
                    signInButton.setText(getResources().getString(R.string.sign_up));
                    signToggleButton.setText(getResources().getString(R.string.sign_in));
                    //Update explanation text above toggle button.
                    needAccountText.setText(getResources().getString(R.string.already_have_account));
                } else {
                    //Change back to sign in layout.
                    //Have animation move edit texts in place.
                    //Move first name to bottom under username
                    Animation firstNameTextAnim = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                                    firstNameText.getLayoutParams();
                            params.topMargin = (int)(2000 * interpolatedTime);
                            firstNameText.setLayoutParams(params);
                        }
                    };
                    firstNameTextAnim.setDuration(300); // in ms
                    firstNameText.startAnimation(firstNameTextAnim);
                    //Have Password move back up.
                    final Animation passwordTextAnim = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                                    passwordText.getLayoutParams();
                            int totalHeight = firstNameText.getMeasuredHeight() + eightDp +
                                    lastNameText.getMeasuredHeight();
                            params.topMargin = (int) (totalHeight-  interpolatedTime *
                                    (totalHeight - eightDp));
                            passwordText.setLayoutParams(params);
                        }
                    };
                    passwordTextAnim.setDuration(300);
                    passwordText.startAnimation(passwordTextAnim);
                    //Have confirmPasswordMove just below password.
                    final Animation confirmPasswordAnim = new Animation(){
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)
                                    confirmPassword.getLayoutParams();
                            params.topMargin = (int) (2000 * interpolatedTime);
                            confirmPassword.setLayoutParams(params);
                        }
                    };
                    confirmPasswordAnim.setDuration(300);
                    confirmPassword.startAnimation(confirmPasswordAnim);
                    ConstraintSet constraints = new ConstraintSet();
                    ConstraintLayout layout = findViewById(R.id.login_layout);
                    constraints.clone(layout);
                    //Have sign in button be just under password field
                    constraints.connect(R.id.sign_in_button, ConstraintSet.TOP,
                            R.id.password_field, ConstraintSet.BOTTOM);
                    constraints.applyTo(layout);
                    signInToggle = true;
                    //Swap button labels.
                    signInButton.setText(getResources().getString(R.string.sign_in));
                    signToggleButton.setText(getResources().getString(R.string.sign_up));
                    //Update explanation text above toggle button.
                    needAccountText.setText(getResources().getString(R.string.need_account));
                }
            }
        });
    }

}