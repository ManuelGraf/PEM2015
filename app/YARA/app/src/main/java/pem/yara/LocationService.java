package pem.yara;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
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

    // TODO Fabi: Visualize one RUN in comparison to all runs on that track ==> ok, now make sure it works!
    // TODO Fabi: Visualize TRACK through Google Maps API

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // We do this via Google Play Services
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener mLocationListener;

    // TODO: Figure out a reasonable interval
    private int recInterval = 1000;
    private ArrayList<Location> aTrack;
    private int secondsRun;

    /*
    Public methods for Clients to call
     */
    public ArrayList<Location> receiveTrack(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        return aTrack;
    }

    /*
    Class methods from here on
     */

    /* Initialize the Location and API services once on Create.
        These services are started as RunActivity starts */
    @Override
    public void onCreate(){
        super.onCreate();
        aTrack = new ArrayList<>();

        // Create a Location Request, ...
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // ... a Listener...
        mLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("LocationListener", "Location: " + location.toString());
                if(aTrack.isEmpty()){
                    aTrack.add(location);
                } else{
                    float tmpDistance=aTrack.get(aTrack.size() - 1).distanceTo(location);
                    Log.d("LocationListener", "User moved for " + tmpDistance + " Meters");
                    if(tmpDistance > 2.f){
                        aTrack.add(location);
                    }
                }

                Log.d("LocationListener", "aTrack now contains " + aTrack.size() + " points.");
                secondsRun++;
            }
        };

        // ... and the Google API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        Log.d("Service onCreate", "Google API Client built");

        // ... and connect.
        mGoogleApiClient.connect();
        Log.d("Service onCreate", "Google API Client connected");
    }


    // Is called by startService() in StartActivity
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        super.onStartCommand(intent, flags, startID);

        Bundle b = intent.getExtras();
        if(b != null && b.containsKey("recInterval")) {
            recInterval = b.getInt("recInterval");
            Log.d("onStartCommand", "Intent got a new recInterval: " + recInterval);
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

        aTrack=null;
        mGoogleApiClient.disconnect();
        Log.d("onDestroy", "Google API Client disconnected");
        Log.d("onDestroy", "Service destroyed");
    }

    // Starts requesting Location updates
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("onConnected", "requesting Location updates...");
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
            // Return this instance of LocationService so clients can call public methods
            return LocationService.this;
        }
    }

}