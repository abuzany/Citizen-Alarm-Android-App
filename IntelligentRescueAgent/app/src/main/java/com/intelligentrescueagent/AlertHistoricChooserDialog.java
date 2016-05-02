package com.intelligentrescueagent;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Created by Angel Buzany on 23/03/2016.
 */
public class AlertHistoricChooserDialog extends DialogFragment implements View.OnClickListener  {

    private final static String SENDER = "AlertHistoricChooserDialog";

    private Comunicator comunicator;

    private RadioGroup mRgAlerts;
    private RadioButton mRbAll;
    private RadioButton mRbToday;
    private RadioButton mRbWeek;
    private RadioButton mRbMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.alert_historic_chooser_dialog, container, false);

        mRgAlerts = (RadioGroup) view.findViewById(R.id.rgAlerts);
        mRbAll = (RadioButton) view.findViewById(R.id.rbAll);
        mRbToday = (RadioButton) view.findViewById(R.id.rbToday);
        mRbWeek = (RadioButton) view.findViewById(R.id.rbWeek);
        mRbMonth = (RadioButton) view.findViewById(R.id.rbMonth);

        Button btnSend = (Button) view.findViewById(R.id.btnOk);
        btnSend.setOnClickListener(this);

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
        if(v.getId() == R.id.btnOk){
            //Get selected radio button from radioGroup
            int rbSelectedId = mRgAlerts.getCheckedRadioButtonId();

            //Sanity check
            if(rbSelectedId == -1){
                Toast.makeText(getActivity(), "Debes seleccionar un criterio", Toast.LENGTH_SHORT).show();
                return;
            }

            String msg;
            switch (rbSelectedId){
                case R.id.rbAll:
                    msg = "1";
                    comunicator.onDialogMessage(SENDER, msg);
                    break;
                case R.id.rbToday:
                    msg = "2";
                    comunicator.onDialogMessage(SENDER, msg);
                    break;
                case R.id.rbWeek:
                    msg = "3";
                    comunicator.onDialogMessage(SENDER, msg);
                    break;
                case R.id.rbMonth:
                    msg = "4";
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
