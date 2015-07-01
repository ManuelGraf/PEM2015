package pem.yara;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import pem.yara.db.RunDbHelper;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraRun;
import pem.yara.entity.YaraTrack;


public class StatisticsActivity extends ActionBarActivity {

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    CameraUpdate mCameraUpdate;

    private RunDbHelper mRunDbHelper;
    private TrackDbHelper mTrackDbHelper;

    // UI Elements:
    private TextView trackTime;
    private TextView trackPace;
    private TextView trackDistance;
    private TextView trackAvgTime;
    private TextView trackAvgSpeed;
    private TextView trackAvgPace;
    private RelativeLayout statisticsContainer;
    private EditText editTrackName;

    private int mTrackID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        int trackID;

        // Read extras, if available
        try {
            trackID = getIntent().getExtras().getInt("TrackID");
            mTrackID = trackID;
            Log.d("Statistics onCreate", "TrackID: " + trackID);
        } catch (Exception e){
            Log.d("Statistics onCreate", "No TrackID passed");
            return;
        }

        // Get UI Elements:

        trackTime = (TextView)findViewById(R.id.track_item_time);
        trackPace = (TextView)findViewById(R.id.track_item_pace);
        trackDistance = (TextView)findViewById(R.id.track_item_distance);
        trackAvgTime = (TextView)findViewById(R.id.track_avg_time);
        trackAvgSpeed = (TextView)findViewById(R.id.track_avg_speed);
        trackAvgPace = (TextView)findViewById(R.id.track_avg_pace);
        statisticsContainer = (RelativeLayout)findViewById(R.id.statisticsContainer);
        editTrackName = (EditText)findViewById(R.id.editTrackName);
        setupParent(statisticsContainer);
        // Enable the user to name his own tracks:
        editTrackName.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            Log.d("editNameListener","user typed new name "+editTrackName.getText().toString());

                            // the user is done typing.
                            mTrackDbHelper.saveTrackName(mTrackID,editTrackName.getText().toString());
                            hideKeyboard();
                            return true; // consume.

                        }
                        return false; // pass on to other listeners.
                    }
                });


        // Initialize Map View and Map itself:
        mMapView = (MapView)findViewById(R.id.googleMapsView);
        mMapView.onCreate(savedInstanceState);
        mGoogleMap = mMapView.getMap();

        // Get Database handlers
        mRunDbHelper = new RunDbHelper(getBaseContext());
        mTrackDbHelper = new TrackDbHelper(getBaseContext());

        // Get given Track
        YaraTrack myTrack = mTrackDbHelper.getTracks(trackID).get(0);
        // Get last Run on that Track
        YaraRun myRun = mRunDbHelper.getLastRunToTrack(trackID);

        // Set Data for this specific run:
        editTrackName.setText(myTrack.getTitle());
        trackTime.setText((int)myRun.getCompletionTime()/60 + ":" + (int)myRun.getCompletionTime()%60 + " min");
        trackPace.setText(myRun.getAvgBpm() + " Steps/min");
        trackDistance.setText(myTrack.getLength() + " Meters");

        // Calculate Statistics over all Runs on this track:
        ArrayList<YaraRun> allRuns = mRunDbHelper.getRuns(-1, myTrack.getId());
        double avgTime = 0.f;
        double avgPace = 0.f;
        double avgSpeed = 0.f;

        // Get means for all Runs on that Track
        for(YaraRun r:allRuns){
            avgTime += r.getCompletionTime();
            avgPace += r.getAvgBpm();
            avgSpeed += r.getAvgSpeed();
        }
        avgTime /= allRuns.size();
        avgPace /= allRuns.size();
        avgSpeed /= allRuns.size();

        // Average Statistics over given Track:
        trackAvgTime.setText((int)avgTime/60 + ":" + (int)avgTime%60 + "min");
        trackAvgSpeed.setText(String.format("%.2f", avgSpeed/3.6) + " km/h");
        trackAvgPace.setText(avgPace + " Steps/min");
        String[] trackPoints;

        // Get list of Locations if possible, else set dummy
        if(myTrack.getPathString().length()>0)
            trackPoints= myTrack.getPathString().split(";", 0);
        else
            trackPoints = new String[]{"49,11"};

        Log.d("StatisticsActivity", "Anzahl Punkte myTrack: " + trackPoints.length);

        // Draw a line along the track points
        PolylineOptions mPolylineOptions = new PolylineOptions().geodesic(true);
        LatLng startPoint = null;
        LatLngBounds.Builder mLatLngBuilder = new LatLngBounds.Builder();

        // Build Strings into Coordinates and Polygon Lines:
        for(int i=0; i<trackPoints.length; i++){
            String s = trackPoints[i];
            Log.d("StatisticsActivity", "Track Point: " + s);
            LatLng tmpCoordinate;
            double thisLat = Double.parseDouble(s.substring(0, s.indexOf(",") - 1));
            double thisLong = Double.parseDouble(s.substring(s.indexOf(",") + 1));

            tmpCoordinate = new LatLng(thisLat, thisLong);
            Log.d("StatisticsActivity", tmpCoordinate.toString());

            if(i==0){
                startPoint = tmpCoordinate;
            }

            mLatLngBuilder.include(tmpCoordinate);
            mPolylineOptions.add(tmpCoordinate);
        }
        LatLngBounds bounds = mLatLngBuilder.build();

        // Add Marker for Start of Run:
        MapsInitializer.initialize(getBaseContext());
        mGoogleMap.addPolyline(mPolylineOptions);
        mGoogleMap.addMarker(new MarkerOptions()
                .title("Run Start")
                .snippet("Length: " + myRun.getRunDistance() + "m, Duration: " + myRun.getCompletionTime() / 1000 / 60 + "min")
                .position(startPoint));

        // Zoom Map on starting point. This needs to be done; otherwise, the map won't load completely (trying to show the whole world, thus taking forever)
        mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        // Zoom in/out on whole track
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));

        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mGoogleMap.animateCamera(mCameraUpdate);
            }
        });

    }
    private void hideKeyboard()
    {
        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    @Override
    protected void onResume(){
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.actionStartRun) {
            startRun();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setupParent(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    mTrackDbHelper.saveTrackName(mTrackID,editTrackName.getText().toString());
                    hideKeyboard();
                    return false;
                }
            });
        }
        //If a layout container, iterate over children
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupParent(innerView);
            }
        }
    }
    public void startRun(){
        Intent intent = new Intent(getBaseContext(), RunActivity.class);
        intent.putExtra("TrackID", mTrackID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(intent);

    }

}
