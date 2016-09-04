package com.intelligentrescueagent.Framework.AIAgent.Tasks.Scheduled;

import com.intelligentrescueagent.Models.User;

import java.util.TimerTask;

import io.socket.client.Socket;

/**
 * Created by abuza on 27/07/2016.
 */
public class CheckSginInTask  extends TimerTask {
    private static final String TAG = "TrackLogTask";

    private Socket mSocket;
    private String mUserId;

    public CheckSginInTask(Socket socket, String userId){
        this.mSocket = socket;
        this.mUserId = userId;
    }

    @Override
    public void run() {
        /*//Avoid to add to tail the requests
        if(isServerConnected()){
            mSocket.emit("onIsUserSignedIn", mUser.getFacebookID());
        }*/
        mSocket.emit("onIsUserSignedIn", mUserId);
    }
}
