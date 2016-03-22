package com.intelligentrescueagent.Framework.Maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by abuza on 20/03/2016.
 */
public class AlarmMark implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public AlarmMark(LatLng postion) {
        mPosition = postion;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public void setSnippet(String snippet) {
        this.mSnippet = snippet;
    }
}
