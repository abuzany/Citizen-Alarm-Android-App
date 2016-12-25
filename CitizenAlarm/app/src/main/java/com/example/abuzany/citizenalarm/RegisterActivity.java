package com.example.abuzany.citizenalarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.abuzany.citizenalarm.framework.networking.http.ObeliskService;
import com.example.abuzany.citizenalarm.models.UserRegister;
import com.example.abuzany.citizenalarm.models.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    //UI References
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (EditText) findViewById(R.id.txtEmail);
        mPasswordView = (EditText) findViewById(R.id.txtPassword);
        mConfirmPasswordView = (EditText) findViewById(R.id.txtConfirPassword);

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempRegister();
            }
        });
    }

    private void attempRegister(){
        try {
            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();
            String confirmPassword = mConfirmPasswordView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password confirmed, if the user entered one.
            if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword) && isPasswordConfirmed(password, confirmPassword)) {
                mPasswordView.setError(getString(R.string.error_no_password_confirmed));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt register and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                //showProgress(true);
                //mAuthTask = new UserLoginTask(email, password);
                //mAuthTask.execute((Void) null);

                UserRegister userRegister = new UserRegister();
                userRegister.setEmail(email);
                userRegister.setPassword(password);
                userRegister.setConfirmPassword(confirmPassword);

                Call<UserResponse> postUserRegisterCall = ObeliskService.getInstance().getService().postUserRegister(userRegister);
                postUserRegisterCall.enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if(response.isSuccessful())
                            openActivity("Main");
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Log.e(TAG, "postUserCall->onFailure: " + t.getMessage());
                    }
                });
            }
        }catch (Exception e){
            Log.e(TAG, "register->"+e.getMessage());
        }
    }

    private void openActivity(String activityName){
        try {
            Intent intent = null;

            switch (activityName){
                case "Main":
                    intent = new Intent(this, MainActivity.class);
                    break;
            }

            startActivity(intent);
        }catch (Exception e){
            Log.e(TAG, "openActivity->" + e.getMessage());
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isPasswordConfirmed(String password, String confirmPassword){
        return password == confirmPassword;
    }
}
