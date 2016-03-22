package com.intelligentrescueagent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;

public class RegisterActivity extends AppCompatActivity{

    private APIService mHTTPClient;
    private User mUser;

    private EditText txtAlias;

    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Retrieve Information
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUserId = extras.getString("userId");
        }

        //Initialize
        mHTTPClient = ServiceGenertor.createService(APIService.class);

        txtAlias = (EditText) findViewById(R.id.txtAlias);

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                mUser = new User();
                mUser.setFacebookID(mUserId);
                mUser.setAlias(txtAlias.getText().toString());
                mUser.setEmail("");
                mUser.setCreationDate(dateFormat.format(date));

                Call<User> postUserCall = mHTTPClient.postUser(mUser);
                postUserCall.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        //Open MainActivity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("userId", mUser.getFacebookID());
                        intent.putExtra("alias", mUser.getAlias());

                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("postAlertCall", "onFailure: " + t.getMessage());
                    }
                });
            }
        });
    }
}

