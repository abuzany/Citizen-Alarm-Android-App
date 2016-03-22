package com.intelligentrescueagent.Framework.Maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by abuza on 20/03/2016.
 */
public class ClusterRender extends DefaultClusterRenderer<AlarmMark> {

    public ClusterRender(Context context, GoogleMap map, ClusterManager<AlarmMark> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(AlarmMark item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getSnippet());
    }

    @Override
    protected void onClusterItemRendered(AlarmMark clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        //here you have access to the marker itself
    }
}
