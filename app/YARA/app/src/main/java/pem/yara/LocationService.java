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
import java.util.Random;

/**
 * Created by Fabian on 18.06.2015.
 */
public class LocationService extends Service implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener mLocationListener;

    private int recInterval = 1500;
    private boolean recording = false;
    private boolean connected = false;
    private ArrayList<Location> aTrack;

    // Public methods for Clients to call
//    public void startRecording(){
//        aTrack = new ArrayList<Location>();
//        recording=true;
//
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
//
//    }

    //
//    public ArrayList<Location> stopRecording(){
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
//        return aTrack;
//    }



    // Class methods from here on

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startID){
//
//        this.recInterval = intent.getExtras().getInt("recInterval");
//        Log.d("onStartCommand", "" + recInterval);
//
//        return 0;
//    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("IBinder", "Service binding...");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        Log.d("onBind", "Google API Client built");
        mGoogleApiClient.connect();
        Log.d("onBind", "Google API Client connected");

        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mGoogleApiClient.disconnect();
        Log.d("onDestroy", "Google API Client disconnected");
        Log.d("onDestroy", "Service destroyed");
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(recInterval);

        mLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(recording) {
                    aTrack.add(location);
                }
                Log.d("LocationListener", "Location Changed: " + location.toString());
            }
        };

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