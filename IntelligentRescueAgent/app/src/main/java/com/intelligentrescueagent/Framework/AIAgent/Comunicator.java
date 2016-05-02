package com.intelligentrescueagent.Framework.AIAgent;

import com.intelligentrescueagent.Framework.Settings.GlobalSettings;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Angel Buzany on 14/04/2016.
 */
public class Comunicator {

    private static Socket socket;

    private static Comunicator instance = null;
    protected Comunicator() {
        // Exists only to defeat instantiation.
    }
    public static Comunicator getInstance() {
        if(instance == null) {
            instance = new Comunicator();

            try{
                socket = IO.socket(GlobalSettings.getInstance().getSocketIOAddress());
                socket.connect();
            }catch(URISyntaxException e){
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public Socket getSocket(){
        return socket;
    }
}
