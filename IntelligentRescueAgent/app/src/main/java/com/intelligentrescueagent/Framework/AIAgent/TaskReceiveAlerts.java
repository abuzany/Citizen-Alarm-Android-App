package com.intelligentrescueagent.Framework.AIAgent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.Task;
import com.intelligentrescueagent.MainActivity;
import com.intelligentrescueagent.Models.Alert;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

/**
 * Created by Angel Buzany on 14/04/2016.
 */
public class TaskReceiveAlerts extends Goal {
    @Override
    public void run() {

        Comunicator.getInstance().getSocket().on("onAlert", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    //Retrieving information
                    JSONObject data = (JSONObject) args[0];
                    Alert alert = new Alert();
                    alert.setAlertType(data.getInt("alertTypeId"));
                    alert.setLatitude(data.getDouble("latitude"));
                    alert.setLongitude(data.getDouble("longitude"));

                    //Build notification content
                    String notiTitle = "";
                    String notiContent = "";

                    switch (alert.getAlertType()){

                        case 1:
                            notiTitle = "Robo Reportado!!!";
                            break;

                        case 2:
                            notiTitle = "Accidente Reportado!!!";
                            break;

                        case 3:
                            notiTitle = "Secuestro Reportado!!!";
                            break;
                    }

                    // Build the notification, setting the group appropriately
                    /*NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
                    notification.setSmallIcon(android.R.drawable.stat_notify_chat);
                    notification.setContentTitle(notiTitle);
                    notification.setContentText(notiContent);
                    notification.setGroup(GROUP_KEY_AlARMS);
                    notification.setTicker("Alerta!!!");
                    notification.setAutoCancel(true);

                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra(String.valueOf(alert.getLatitude()), "alertLatitude");
                    intent.putExtra(String.valueOf(alert.getLongitude()), "alertLongitude");

                    PendingIntent pendientIntent =  PendingIntent.getActivity(mContext, 0, intent, 0);

                    notification.setContentIntent(pendientIntent);

                    // Issue the notification
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(10, notification.build());*/

                } catch (JSONException e) {
                    Log.e("Agent", "onAlert: Error retrieving information, " + e.getMessage());
                }catch (Exception e){
                    Log.e("Agent", "onAlert: " + e.getMessage());
                }
            }
        });
    }
}
