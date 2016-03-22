package com.intelligentrescueagent.Framework.AIAgent;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by LENOVO on 10/01/2016.
 */
public class AgentTask implements Runnable {

    public AgentTask(){
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000);

                Log.d("AgentTask", "Report");

            } catch (InterruptedException e) {
                Log.e("AgentTask", e.getMessage());
            }
        }
    }
}
