package com.intelligentrescueagent;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Angel Buzany on 23/03/2016.
 */
public class AlertChooserDialog extends DialogFragment implements View.OnClickListener {

    private final static String SENDER = "AlertChooserDialog";

    private Comunicator comunicator;

    private EditText mTxtAlertDescription;
    private TextView mTVCharCount;
    private Button mButtonSend;
    private RadioGroup mRgAlerts;
    private RadioButton mRbRobery;
    private RadioButton mRbAccident;
    private RadioButton mRbKidnap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.alert_chooser_dialog, container, false);

        mTxtAlertDescription = (EditText) view.findViewById(R.id.txtAlertDescription);
        mTVCharCount = (TextView) view.findViewById(R.id.tvCharCount);
        mRgAlerts = (RadioGroup) view.findViewById(R.id.rgAlerts);
        mRbRobery = (RadioButton) view.findViewById(R.id.rbRobery);
        mRbAccident = (RadioButton) view.findViewById(R.id.rbAccident);
        mRbKidnap = (RadioButton) view.findViewById(R.id.rbKidnap);
        mButtonSend = (Button) view.findViewById(R.id.btnSendAlert);

        mTxtAlertDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int maxChar = 140;
                int result = maxChar - s.length();

                mTVCharCount.setText(String.valueOf(result)+"/140");

                if(result <= 0)
                    mTVCharCount.setTextColor(ColorStateList.valueOf(Color.RED));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mButtonSend.setOnClickListener(this);

        // Inflate the layout to use as dialog or embedded fragment
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        comunicator = (Comunicator) activity;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSendAlert){
            //Get selected radio button from radioGroup
            int rbSelectedId = mRgAlerts.getCheckedRadioButtonId();
            String alertDescription = mTxtAlertDescription.getText().toString();

            //Sanity check
            if(rbSelectedId == -1){
                Toast.makeText(getActivity(), "Debes seleccionar una alerta", Toast.LENGTH_SHORT).show();
                return;
            }

            if(alertDescription.equals("")){
                Toast.makeText(getActivity(), "Debes agregar una decripción", Toast.LENGTH_SHORT).show();
                return;
            }

            String msg;
            switch (rbSelectedId){
                case R.id.rbRobery:
                    msg = "1|"+alertDescription;
                    comunicator.onDialogMessage(SENDER, msg);
                    break;
                case R.id.rbAccident:
                    msg = "2|"+alertDescription;
                    comunicator.onDialogMessage(SENDER, msg);
                    break;
                case R.id.rbKidnap:
                    msg = "3|"+alertDescription;
                    comunicator.onDialogMessage(SENDER, msg);
                    break;
            }

            dismiss();
        }
    }

    interface Comunicator{
        public void onDialogMessage(String sender, String msg);
    }
}
