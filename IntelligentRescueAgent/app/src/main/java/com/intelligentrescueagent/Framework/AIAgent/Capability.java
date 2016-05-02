package com.intelligentrescueagent.Framework.AIAgent;

/**
 * Created by Angel Buzany on 14/04/2016.
 */
public class Capability {

    private Goal mGoal;

    private String mName;
    private Boolean mEnabled;

    public Capability(String name){
        mName = name;
    }

    public void setGoal(Goal goal){
        mGoal = goal;
    }

    public Goal getGoal() {
        return mGoal;
    }
}
