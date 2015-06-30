package pem.yara;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import pem.yara.db.RunDbHelper;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraRun;
import pem.yara.entity.YaraTrack;


public class StatisticsActivity extends ActionBarActivity implements OnMapReadyCallback {

    private MapFragment mMapFragment;
    private RunDbHelper mRunDbHelper;
    private TrackDbHelper mTrackDbHelper;

    // UI Elements:
    private TextView trackName;
    private TextView trackTime;
    private TextView trackPace;
    private TextView trackDistance;
    private TextView trackAvgTime;
    private TextView trackAvgSpeed;
    private TextView trackAvgPace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        int trackID;

        // Read extras, if available
        try {
            trackID = getIntent().getExtras().getInt("TrackID");
            Log.d("Statistics onCreate", "TrackID: " + trackID);
        } catch (Exception e){
            Log.d("Statistics onCreate", "No TrackID passed");
            return;
        }

        // Get UI Elements:
        trackName = (TextView)findViewById(R.id.track_item_name);
        trackTime = (TextView)findViewById(R.id.track_item_time);
        trackPace = (TextView)findViewById(R.id.track_item_pace);
        trackDistance = (TextView)findViewById(R.id.track_item_distance);
        trackAvgTime = (TextView)findViewById(R.id.track_avg_time);
        trackAvgSpeed = (TextView)findViewById(R.id.track_avg_speed);
        trackAvgPace = (TextView)findViewById(R.id.track_avg_pace);

        mRunDbHelper = new RunDbHelper(getBaseContext());
        mTrackDbHelper = new TrackDbHelper(getBaseContext());

        // Get given Track
        YaraTrack myTrack = mTrackDbHelper.getTracks(trackID).get(0);
        // Get last Run on that Track
        YaraRun myRun = mRunDbHelper.getLastRunToTrack(trackID);

        // Set Data for this specific run:
        trackName.setText(myTrack.getTitle());
        trackTime.setText(myRun.getCompletionTime() + "min");
        trackPace.setText(myRun.getAvgBpm() + " Steps/min");
        trackDistance.setText(myTrack.getLength() + " Meters");

        // Calculate Statistics over all Runs on this track:
        ArrayList<YaraRun> allRuns = mRunDbHelper.getRuns(-1, myTrack.getId());
        float avgTime = 0.f;
        float avgPace = 0.f;
        float avgSpeed = 0.f;

        // Get means for all Runs on that Track
        for(YaraRun r:allRuns){
            avgTime += r.getCompletionTime();
            avgPace += r.getAvgBpm();
            avgSpeed += r.getAvgSpeed();
        }
        avgTime /= allRuns.size();
        avgPace /= allRuns.size();
        avgSpeed /= allRuns.size();

        trackAvgTime.setText(avgTime/1000/60 + "min");
        trackAvgSpeed.setText(avgSpeed/3.6 + " km/h");
        trackAvgPace.setText(avgPace + " Steps/min");


//        mMapFragment = (MapFragment) getFragmentManager()
//                .findFragmentById(R.id.googleMapsView);
//        mMapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d("onmapReady", "Map ready");
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.920656, 11.424632), 2));
//
//        map.addPolyline(new PolylineOptions().geodesic(true)
//                .add(new LatLng(47.920656, 11.424632))  // Home
//                .add(new LatLng(47.920657, 11.424633))  // Home
//                .add(new LatLng(47.920658, 11.424634))  // Home
//                .add(new LatLng(47.920659, 11.424635))  // also Home
//        );
    }
}
