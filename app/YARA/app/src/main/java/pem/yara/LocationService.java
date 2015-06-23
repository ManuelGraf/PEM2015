package pem.yara;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class LocationService extends Service implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // We do this via Google Play Services
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener mLocationListener;

    private int recInterval = 1500;
    private boolean recording = false;
    private ArrayList<Location> aTrack;

    /*
    Public methods for Clients to call
     */

    /**
     * Record a List of Locations
     */
    public void startRecording(){
        aTrack = new ArrayList<Location>();
        recording=true;

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

    }

    //
    public ArrayList<Location> stopRecording(){
        recording=false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        return aTrack;
    }

    public boolean isAPIConnected(){
        return mGoogleApiClient.isConnected();
    }

    /*
    Class methods from here on
     */

    @Override
    public void onCreate(){
        super.onCreate();


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(recording) {
                    aTrack.add(location);
                }
                Log.d("LocationListener", "aTrack contains " + ((aTrack==null) ? 0 : aTrack.size()) + " points.");
                Log.d("LocationListener", "Location Changed: " + location.toString());
            }
        };

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        Log.d("Service onCreate", "Google API Client built");
        mGoogleApiClient.connect();
        Log.d("Service onCreate", "Google API Client connected");
    }

    // Is called by startService() in StartActivity
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        super.onStartCommand(intent, flags, startID);

        Log.d("onStartCommand", "Old Interval: " + recInterval);
        Bundle b = intent.getExtras();

        if(b.containsKey("recInterval")) {
            recInterval = b.getInt("recInterval");
            Log.d("onStartCommand", "Intent contains new recInterval: " + recInterval);
        } else {
            Log.d("onStartCommand", "Intent doesn't contain recInterval. Standard value: " + recInterval);
        }

        mLocationRequest.setInterval(recInterval);

        return 0;
    }

    // Called by bindService() in StartActivity
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("IBinder", "Service binding...");
        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mGoogleApiClient.disconnect();
        Log.d("onDestroy", "Google API Client disconnected");
        Log.d("onDestroy", "Service destroyed");
    }

    // Starts requesting Location updates
    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("onConnectionFailed", "GoogleApiClient connection has failed");
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

}