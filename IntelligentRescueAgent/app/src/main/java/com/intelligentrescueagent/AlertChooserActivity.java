package com.intelligentrescueagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Angel Buzany on 22/01/2016.
 */
public class AlertChooserActivity extends Activity{

    private Button btnRobery;
    private Button btnAccident;
    private Button btnKidnap;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_chooser);

        //Set window dimension

        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .6));

        //Initialize ui controls

        this.tvDescription = (TextView) findViewById(R.id.tvDescription);

        btnRobery = (Button) findViewById(R.id.btnRobery);
        btnRobery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent data = new Intent();
                data.putExtra("alertTypeId", 1);
                data.putExtra("alertDescription", tvDescription.getText().toString());

                setResult(RESULT_OK, data);

                finish();
            }
        });

        this.btnAccident = (Button) findViewById(R.id.btnAccident);
        this.btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("alertTypeId", 2);
                data.putExtra("alertDescription", tvDescription.getText().toString());

                setResult(RESULT_OK, data);

                finish();
            }
        });

        this.btnKidnap = (Button) findViewById(R.id.btnKidnap);
        this.btnKidnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("alertTypeId", 3);
                data.putExtra("alertDescription", tvDescription.getText().toString());

                setResult(RESULT_OK, data);

                finish();
            }
        });
    }
}
