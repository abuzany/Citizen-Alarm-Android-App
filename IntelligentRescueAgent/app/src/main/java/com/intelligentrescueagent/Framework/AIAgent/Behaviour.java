package com.intelligentrescueagent.Framework.AIAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abuza on 14/04/2016.
 */
public class Behaviour {

    private ArrayList<Capability> mCapabilityList;

    private String mName;

    public Behaviour(String name){
        mName = name;

        mCapabilityList = new ArrayList<Capability>();
    }

    public void addCapability(Capability capability){
        mCapabilityList.add(capability);
    }

    public void init(){
        for(Capability cap: mCapabilityList){
            cap.getGoal().run();
        }
    }
}
