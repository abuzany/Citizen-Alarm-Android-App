package com.intelligentrescueagent;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.intelligentrescueagent.Framework.AIAgent.Agent;
import com.intelligentrescueagent.Framework.DataBase.DataBaseHelper;
import com.intelligentrescueagent.Framework.GPS.GPSTracker;
import com.intelligentrescueagent.Framework.Maps.AlertMarker;
import com.intelligentrescueagent.Framework.Maps.ClusterRender;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Framework.Settings.GlobalSettings;
import com.intelligentrescueagent.Models.Alert;
import com.intelligentrescueagent.Models.KatinkResponse;
import com.intelligentrescueagent.Models.User;
import com.intelligentrescueagent.Models.UserConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buzany on 06/01/2016.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
                   AlertChooserDialog.Comunicator, AlertHistoricChooserDialog.Comunicator,
                   Agent.ServerListener{

    private static final int SETTINGS_RESULT = 1;
    private static final String TAG = "MainActivity";

    public static boolean IsRuning = false;

    private GoogleMap mMap;
    private GPSTracker mGPS;
    //private Agent mAgentService;
    private Marker mUserMarker;
    private APIService mHTTPClient;
    private ClusterManager<AlertMarker> mClusterManager;
    private HashMap<Marker, String> mMarkers;
    private User mUser;

    private boolean mIsBound = false;
    private double mLatitude;
    private double mLongitude;

    ///////////////////////////////////////////////Events///////////////////////////////////////////
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
                showAlertChooserDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Initialize objects and variables
        mHTTPClient = ServiceGenertor.createService(APIService.class);
        mMarkers = new HashMap<Marker, String>();

        //Retrieve information from previous activity
        mUser = (User) getIntent().getSerializableExtra("user");

        //Get current location
        mGPS = new GPSTracker(MainActivity.this);

        //Check if GPS enabled
        if (mGPS.canGetLocation()) {
            mLatitude = mGPS.getLatitude();
            mLongitude = mGPS.getLongitude();
        } else {
            mGPS.showSettingsAlert();
        }

        //Set UI Information
        TextView tvAlias = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tvAlias);
        tvAlias.setText(mUser.getAlias());

        TextView tvEmail = (TextView)navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        tvEmail.setText(mUser.getEmail());

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getUserConfigurations();

        IsRuning = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to Agent service
        Intent intent = new Intent(this, Agent.class);
        intent.putExtra("action","up");
        intent.putExtra("userId", mUser.getFacebookID());
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            //unbindService(mConnection);
            mIsBound = false;
        }

        IsRuning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mIsBound) {
            //unbindService(mConnection);
            mIsBound = false;
        }
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
        //getMenuInflater().inflate(R.menu.main22, menu);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_start) {
            cleanMap();
            getTodayAlerts();
        } else if (id == R.id.nav_myalerts) {
            cleanMap();
            showAlertHistoricChooserDialog();
        } else if (id == R.id.nav_configurations) {
            showUserConfigurationsDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        mClusterManager = new ClusterManager<AlertMarker>(this, mMap);
        mClusterManager.setRenderer(new ClusterRender(this, mMap, mClusterManager));
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        // Setting a custom info window adapter for the google map
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.alert_marker, null);

                TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
                TextView tvDescription = (TextView) v.findViewById(R.id.tvDescription);
                TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
                TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);

                String snnipet = marker.getSnippet();

                if(snnipet != null){
                    String[] data = snnipet.split("\\|");

                    //Setting data
                    tvTitle.setText(marker.getTitle());
                    tvDescription.setText(data[0]);
                    tvDate.setText(data[1]);
                    tvAddress.setText(data[2]);

                    return v;
                }
                else{
                    return null;
                }
            }
        });

        //Load Alerts
        getTodayAlerts();
    }

    @Override
    public void onDialogMessage(String sender, String msg) {

        if(sender.equalsIgnoreCase("AlertChooserDialog")){
            //Parse information
            String[] data = msg.split("\\|");

            int alertType = Integer.parseInt(data[0]);
            String alertDescription = data[1];
            LatLng alertPosition = mUserMarker.getPosition();

            //Save alert through APIRest
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            Alert alert = new Alert();
            alert.setUserId(mUser.getFacebookID());
            alert.setAlertType(alertType);
            alert.setCreationDate(dateFormat.format(date));
            alert.setDescription(alertDescription);
            alert.setLatitude(alertPosition.latitude);
            alert.setLongitude(alertPosition.longitude);

            addAlertMarker(alert);

            //mAgentService.sendAlert(alert);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SETTINGS_RESULT)
            displayUserSettings();
    }

    /////////////////////////////////////Agent Events///////////////////////////////////////////////
    @Override
    public void onAlertReceived(final KatinkResponse kr) {
        Handler mainHandler = new Handler(this.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Alert alert = (Alert) kr.getContent();

                addAlertMarker(alert);

                LatLng alertLatLng = new LatLng(alert.getLatitude(), alert.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alertLatLng, 50));
            }
        });
    }

    @Override
    public void onSignedIn(KatinkResponse kr){
    }

    ////////////////////////////////////////Methods/////////////////////////////////////////////////
    /*private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to Agent, cast the IBinder and get LocalService instance
            Agent.AgentBinder binder = (Agent.AgentBinder) service;
            mAgentService = binder.getService();
            mIsBound = true;

            //Set Agent information
            mAgentService.setUser(mUser);
            mAgentService.setContext(MainActivity.this);
            mAgentService.agentUp();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
        }
    };*/

    private void getTodayAlerts(){
        createUserMarker();

        Call<List<Alert>> getAlertsCall = mHTTPClient.getTodayAlerts();
        getAlertsCall.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                if(response.isSuccess()){
                    List<Alert> alertList = response.body();

                    if(alertList != null){
                        if(alertList.size() > 0){
                            for (Alert alert : alertList){
                                addAlertMarker(alert);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                Log.d(TAG, "GetTodayAlerts->onFailure" + t.getMessage());
            }
        });
    }

    private void getUserAlerts(){
        Call<List<Alert>> getUserAlertsCall = mHTTPClient.getUserAlerts(mUser.getFacebookID());
        getUserAlertsCall.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                if(response.isSuccess()){
                    List<Alert> alertList = response.body();
                    if(alertList != null){
                        if(alertList.size() > 0){
                            for (Alert alert : alertList)
                                addAlertMarker(alert);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                Log.d(TAG, "GetUserAlerts->onFailure" + t.getMessage());
            }
        });
    }

    private void getTodayUserAlerts(){
        Call<List<Alert>> getUserAlertsCall = mHTTPClient.getTodayUserAlerts(mUser.getFacebookID());
        getUserAlertsCall.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                if(response.isSuccess()){
                    List<Alert> alertList = response.body();
                    if(alertList != null){
                        if(alertList.size() > 0){
                            for (Alert alert : alertList)
                                addAlertMarker(alert);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                Log.d(TAG, "GetUserAlerts->onFailure" + t.getMessage());
            }
        });
    }

    private void getWeekUserAlerts(){
        Call<List<Alert>> getUserAlertsCall = mHTTPClient.getWeekUserAlerts(mUser.getFacebookID());
        getUserAlertsCall.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                if (response.isSuccess()) {
                    List<Alert> alertList = response.body();
                    if (alertList != null) {
                        if (alertList.size() > 0) {
                            for (Alert alert : alertList)
                                addAlertMarker(alert);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                Log.d(TAG, "GetUserAlerts->onFailure" + t.getMessage());
            }
        });
    }

    private void getMonthUserAlerts(){
        Call<List<Alert>> getUserAlertsCall = mHTTPClient.getMonthUserAlerts(mUser.getFacebookID());
        getUserAlertsCall.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                if (response.isSuccess()) {
                    List<Alert> alertList = response.body();
                    if (alertList != null) {
                        if (alertList.size() > 0) {
                            for (Alert alert : alertList)
                                addAlertMarker(alert);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                Log.d(TAG, "GetUserAlerts->onFailure" + t.getMessage());
            }
        });
    }

    private void addAlertMarker(Alert alert){
        LatLng position = new LatLng(alert.getLatitude(), alert.getLongitude());

        AlertMarker am = new AlertMarker(position);

        switch (alert.getAlertType()){
            case 1:
                am.setTitle(getString(R.string.robbery));
                break;
            case 2:
                am.setTitle(getString(R.string.accident));
                break;
            case 3:
                am.setTitle(getString(R.string.kidnapping));
                break;
        }

        String snippet = alert.getDescription() + "|" + alert.getCreationDate() +  "|" + alert.getAddress();

        am.setSnippet(snippet);

        mClusterManager.addItem(am);
        mClusterManager.cluster();
    }

    private void cleanMap(){
        mMap.clear();
        mClusterManager.clearItems();
    }

    private void showAlertChooserDialog() {
        DialogFragment dialog = new AlertChooserDialog();
        dialog.show(getFragmentManager(), "Alert Chooser");
    }

    private void showAlertHistoricChooserDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.historic_titlte_dialog)
                .items(R.array.chosePeriod)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                getUserAlerts();
                                break;
                            case 1:
                                getTodayUserAlerts();
                                break;
                            case 2:
                                getWeekUserAlerts();
                                break;
                            case 3:
                                getMonthUserAlerts();
                                break;
                        }

                        return true;
                    }
                })
                .positiveText("Ok")
                .show();
    }

    private void showUserConfigurationsDialog() {
        Intent i = new Intent(getApplicationContext(), UserConfigurationActivity.class);
        startActivityForResult(i, SETTINGS_RESULT);
    }

    private void displayUserSettings() {
        Call<UserConfiguration> getUserConfigurationByFbId = mHTTPClient.getUserConfigurationByFbId(mUser.getFacebookID());
        getUserConfigurationByFbId.enqueue(new Callback<UserConfiguration>() {
            @Override
            public void onResponse(Call<UserConfiguration> call, Response<UserConfiguration> response) {
                UserConfiguration usrConfiguration = response.body();

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                usrConfiguration.setEnabledNotifications(sharedPrefs.getBoolean("prefEnabledNotification", false));
                usrConfiguration.setRange((double) sharedPrefs.getInt("pregRange", 1));

                //mAgentService.updateUserConfiguration(usrConfiguration);
            }

            @Override
            public void onFailure(Call<UserConfiguration> call, Throwable t) {
                Log.e(TAG, "getUserConfigurationByFbId->onFailure: " + t.getMessage());
            }
        });
    }

    private void getUserConfigurations(){
        Call<UserConfiguration> getUserConfigurationByFbId = mHTTPClient.getUserConfigurationByFbId(mUser.getFacebookID());
        getUserConfigurationByFbId.enqueue(new Callback<UserConfiguration>() {
            @Override
            public void onResponse(Call<UserConfiguration> call, Response<UserConfiguration> response) {
                UserConfiguration usrConfiguration = response.body();

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                SharedPreferences.Editor edit = sharedPrefs.edit();
                edit.putBoolean("prefEnabledNotification", usrConfiguration.isEnabledNotifications());
                edit.putInt("pregRange", (int) usrConfiguration.getRange());
                edit.apply();
            }

            @Override
            public void onFailure(Call<UserConfiguration> call, Throwable t) {
                Log.e(TAG, "getUserConfigurations->onFailure: " + t.getMessage());
            }
        });
    }

    private void createUserMarker(){
        LatLng currentLocation = new LatLng(mLatitude, mLongitude);

        // Add a marker at current location
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mUserMarker = mMap.addMarker(markerOptions);

        mMarkers.put(mUserMarker, "-1");
    }

    private void signout(){
        GlobalSettings.getInstance().getmLoginManager().logOut();

        Intent intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
    }
}
