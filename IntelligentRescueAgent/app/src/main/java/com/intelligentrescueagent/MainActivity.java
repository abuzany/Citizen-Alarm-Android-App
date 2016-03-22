package com.intelligentrescueagent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.intelligentrescueagent.Framework.AIAgent.Agent;
import com.intelligentrescueagent.Framework.GPS.GPSTracker;
import com.intelligentrescueagent.Framework.Maps.AlarmMark;
import com.intelligentrescueagent.Framework.Maps.ClusterRender;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.Alert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final int REQUEST_CODE = 924;

    private GoogleMap mMap;
    private GPSTracker mGPS;
    private Agent mAgentService;
    private Marker mUserMarker;
    private APIService mHTTPClient;
    private ClusterManager<AlarmMark> mClusterManager;

    private boolean mIsBound = false;
    private double mLatitude;
    private double mLongitude;
    private String mUserId;
    private String mAlias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent alertChooserIntent = new Intent(MainActivity.this, AlertChooserActivity.class);

                startActivityForResult(alertChooserIntent, REQUEST_CODE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Initialize
        mHTTPClient = ServiceGenertor.createService(APIService.class);

        //Retrieve information
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUserId = extras.getString("userId");
            mAlias = extras.getString("alias");
        }

        //Get current location
        mGPS = new GPSTracker(MainActivity.this);

        //Check if GPS enabled
        if (mGPS.canGetLocation()) {
            mLatitude = mGPS.getLatitude();
            mLongitude = mGPS.getLongitude();
        } else {
            //Can't get location
            //GPS or Network is not enabled
            //Ask user to enable GPS/network in settings
            mGPS.showSettingsAlert();
        }

        //Set UI Information
        TextView tvAlias = (TextView) findViewById(R.id.tvAlias);
        tvAlias.setText("wew");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main22, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Init Agent
        //Bind to Agent service
        Intent intent = new Intent(this, Agent.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (mIsBound) {
            //Set Agent information
            mAgentService.setLatitude(mLatitude);
            mAgentService.setLongitude(mLongitude);
            mAgentService.setUserId(mUserId);
            mAgentService.setMapsActivity(MainActivity.this);

            if (!mAgentService.isServerConnected())
                mAgentService.connectToRemoteServer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsBound) {
            //Set Agent information
            mAgentService.setLatitude(mLatitude);
            mAgentService.setLongitude(mLongitude);
            mAgentService.setUserId(mUserId);
            mAgentService.setMapsActivity(this);

            if (!mAgentService.isServerConnected())
                mAgentService.connectToRemoteServer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Unbind from the service
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Configure map
        LatLng currentLocation = new LatLng(mLatitude, mLongitude);

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<AlarmMark>(this, mMap);
        mClusterManager.setRenderer(new ClusterRender(this, mMap, mClusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        // Add a marker in current location
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        markerOptions.title("Tu ubicación actual");
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mUserMarker = mMap.addMarker(markerOptions);
        mUserMarker.showInfoWindow();

        //Load Alerts
        GetAlerts();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data.hasExtra("alertTypeId")) {
                //Check if GPS enabled
                if (mGPS.canGetLocation()) {
                    mLatitude = mGPS.getLatitude();
                    mLongitude = mGPS.getLongitude();
                } else {
                    //Can't get location
                    //GPS or Network is not enabled
                    //Ask user to enable GPS/network in settings
                    mGPS.showSettingsAlert();
                }

                int alertType = data.getExtras().getInt("alertTypeId");
                String alertDescription = data.getExtras().getString("alertDescription");

                LatLng alertPosition = mUserMarker.getPosition();

                mAgentService.sendAlert(alertType, alertPosition.latitude, alertPosition.longitude);

                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    //get current date
                    Date date = new Date();

                    Alert alert = new Alert();
                    alert.setUserId(mUserId);
                    alert.setAlertType(alertType);
                    alert.setCreationDate(dateFormat.format(date));
                    alert.setDescription(alertDescription);
                    alert.setLatitude(alertPosition.latitude);
                    alert.setLongitude(alertPosition.longitude);

                    Call<Alert> postAlertCall = mHTTPClient.postAlert(alert);
                    postAlertCall.enqueue(new Callback<Alert>() {
                        @Override
                        public void onResponse(Call<Alert> call, Response<Alert> response) {
                            Log.d("postAlertCall", "onResponse: ");
                        }

                        @Override
                        public void onFailure(Call<Alert> call, Throwable t) {
                            Log.e("postAlertCall", "onFailure: " + t.getMessage());
                        }
                    });

                    Call<List<Alert>> getAlertsCall = mHTTPClient.getAlerts();
                    getAlertsCall.enqueue(new Callback<List<Alert>>() {
                        @Override
                        public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                            List<Alert> alertList = response.body();
                        }

                        @Override
                        public void onFailure(Call<List<Alert>> call, Throwable t) {
                            Log.d("daad", "ffdsf");
                        }
                    });
                } catch (Exception e) {
                    Log.e("Retrofit", e.getMessage());
                }

                //Add a marker in alert reported
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(alertPosition);
                markerOptions.title("Alerta");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(markerOptions).showInfoWindow();
            }
        }
    }

    //Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to Agent, cast the IBinder and get LocalService instance
            Agent.AgentBinder binder = (Agent.AgentBinder) service;
            mAgentService = binder.getService();
            mIsBound = true;

            //Set Agent information
            mAgentService.setLatitude(mLatitude);
            mAgentService.setLongitude(mLongitude);
            mAgentService.setUserId(mUserId);
            mAgentService.setMapsActivity(MainActivity.this);

            if (!mAgentService.isServerConnected())
                mAgentService.connectToRemoteServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
        }
    };

    private void GetAlerts(){
        Call<List<Alert>> getAlertsCall = mHTTPClient.getAlerts();
        getAlertsCall.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {

                if(response.isSuccess()){
                    List<Alert> alertList = response.body();

                    if(alertList != null){
                        if(alertList.size() > 0){
                            for (Alert alert : alertList){
                                LatLng position = new LatLng(alert.getLatitude(), alert.getLongitude());

                                AlarmMark alarmMark = new AlarmMark(position);

                                switch (alert.getAlertType()){
                                    case 1:
                                        alarmMark.setTitle("Robo");
                                        break;
                                    case 2:
                                        alarmMark.setTitle("Accidente");
                                        break;
                                    case 3:
                                        alarmMark.setTitle("Secuestro");
                                        break;
                                }

                                alarmMark.setSnippet(alert.getCreationDate());

                                mClusterManager.addItem(alarmMark);
                            }
                        }
                    }
                }
                else{
                    Log.e("getAlertsCall", "onResponse: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                Log.d("getAlertsCall", "onFailure" + t.getMessage());
            }
        });
    }
}
