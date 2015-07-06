package pem.yara;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import pem.yara.adapters.SongListItemAdapter;
import pem.yara.db.RunDbHelper;
import pem.yara.db.SongDbHelper;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraRun;
import pem.yara.music.AudioPlayer;
import pem.yara.step.StepAccelerometer;
import pem.yara.step.StepCounter;
import pem.yara.step.StepDetection;

public class RunActivity extends ActionBarActivity implements SongChangedListener {

    private int mTrackID;
    private PowerManager.WakeLock mWakeLock;

    LocationService mService;
    Intent locationIntent;

    private ServiceConnection serviceConnection = new AudioPlayerServiceConnection();
    private AudioPlayer audioPlayer;
    private Intent audioPlayerIntent;

    // Songlist Fragment
    private SongListItemAdapter mSongListAdapter;
    private ListView mSongList;
    private SwipeRefreshLayout refreshLayout;

    private ImageButton btnSongControlSkip;
    private ImageButton btnSongControlPause;
    private ImageButton btnSongControlPlay;
    private ImageButton btnSongControlRefresh;

    // run overview part obendrueber
    private Handler timerHandler = new Handler();

    private TextView    txtStepCountPerMinute;
    private TextView    txtTime;
    private TextView    txtDistance;

    private RunDbHelper mRunDbHelper;
    private TrackDbHelper mTrackDbHelper;


    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private StepDetection mStepDetector;

    private Handler handler;
    //in ms
    private int intervalDuration;
    private int intervalFactor;
    private boolean minute;

    private int runningBPM;
    private int currentBPM;
    private double mLastBPM;
    private int threshold;

    private int timesOver;
    private int timesUnder;
    private int timesMax;
    private boolean changeSpeed;

    private ArrayList<Integer> BPMList;
    private int intervalSteps[];
    private int index;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocationService, cast the IBinder and get LocationService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            Log.d("onServiceConnected", "after binder.getService()");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("onServiceDisconnected", "onServiceDisconnected");
        }
    };


    private final class AudioPlayerServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            audioPlayer = ((AudioPlayer.AudioPlayerBinder) baBinder).getService();
            Log.v("StartActivity", "AudioPlayerServiceConnection: Service connected");

            newPlaylist(mLastBPM);
            startService(audioPlayerIntent);
            audioPlayer.setSongChangedListener(RunActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("StartActivity", "AudioPlayerServiceConnection: Service disconnected");
            audioPlayer = null;
        }
    }

    /**
     * Starts and binds the services for Location and Music.
     * Gets a TrackID from the Intent this was started with and selects a playlist based on the average BPM of that Track.
     */
    @Override
    protected void onStart(){
        super.onStart();
        // Bind Service
        Log.d("onStart", "Attempting to bind Service");
        locationIntent = new Intent(this, LocationService.class);

        Context c;
        c=this.getBaseContext();


        // Get TrackID from Intent
        double lastBPM = 0;
        try {
            mTrackID = getIntent().getExtras().getInt("TrackID");

            // Get average BPM to that Track OR start with fixed Value
            YaraRun mYaraRun = mRunDbHelper.getLastRunToTrack(mTrackID);
            if(mYaraRun==null){
                mLastBPM = 104;   // Startwert
            } else {
                mLastBPM = mYaraRun.getAvgBpm();
            }

            Log.d("Run onCreate", "TrackID: " + mTrackID);
        } catch (Exception e){
            e.printStackTrace();
            Log.d("Run onCreate", "No TrackID passed, new track!");
            mTrackID = -1;
        }

        // Bind services to have callable objects
        c.startService(locationIntent);
        c.bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("RunActivity onStart", "LocationService bound");

        audioPlayerIntent = new Intent(this, AudioPlayer.class);
        startService(audioPlayerIntent);
        bindService(audioPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("RunActivity onStart", "AudioService bound");

    }

    /**
     * Wrapper for finishing a run
     */
    private void finishMe(){
        this.finish();
    }

    @Override
    public void onSongChanged(){
        mSongListAdapter = new SongListItemAdapter(getBaseContext(),audioPlayer.getPlayList());
       refreshSongList();
    }

    private void newPlaylist(double bpm){

//        mFragmentSongList = SongListFragment.newInstance(bpm,-1);
//        mFragmentManager.beginTransaction().replace(R.id.songListFragment,mFragmentSongList).commit();
        //TODO: nullpointer bei Aufruf
        audioPlayer.adjustPlaylist(bpm);
       refreshSongList();
    }

    private void refreshSongList(){
        mSongListAdapter = new SongListItemAdapter(getBaseContext(),audioPlayer.getPlayList());
        if(audioPlayer.getPlayList().size() > 0){
            mSongListAdapter.setCurrentSong(audioPlayer.getCurrentSong().getId());
        }
        mSongList.setAdapter(mSongListAdapter);

        ((BaseAdapter)mSongList.getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        txtStepCountPerMinute = (TextView)findViewById(R.id.txtStepCountPerMinute);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance =(TextView)findViewById(R.id.txtDistance);

        // Acquire a partial WakeLock, to keep the background services running even if the phone locks the screen
        PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        mWakeLock.acquire();

        //Songlist itneraction kram

        mSongList = (ListView)findViewById(R.id.songListView);
        mSongListAdapter=new SongListItemAdapter(getBaseContext(),new SongDbHelper(getBaseContext()).getAllSongs());
        mSongList.setAdapter(mSongListAdapter);
        mSongList.setEmptyView(findViewById(R.id.txtSonglistBpmEmpty));

        btnSongControlSkip = (ImageButton)findViewById(R.id.btnSongListControlSkip);
        btnSongControlSkip.setOnClickListener(skipSongListener);
        btnSongControlPause = (ImageButton)findViewById(R.id.btnSongListControlStop);
        btnSongControlPause.setOnClickListener(pauseSongListener);
        btnSongControlPlay = (ImageButton)findViewById(R.id.btnSongListControlPlay);
        btnSongControlPlay.setOnClickListener(playSongListener);
        btnSongControlRefresh = (ImageButton)findViewById(R.id.btnSongListControlRefresh);
        btnSongControlRefresh.setOnClickListener(refreshSongListener);

        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        refreshLayout.setOnRefreshListener(swipeRefresh);
        //mFragmentSongList = SongListFragment.newInstance(80,-1);
        //mFragmentManager.beginTransaction().add(R.id.songListFragmentContainer,mFragmentSongList).commit();

        // Gain access to the Database
        mRunDbHelper = new RunDbHelper(getBaseContext());
        mTrackDbHelper = new TrackDbHelper(getBaseContext());

        /**
         * Init StepDetector
         * Checks if TYPE_STEP_COUNTER is available otherwise fallback via Accelerometer is used to count steps.
         */
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }catch (Exception e){
            Log.d("Step Counter Type", "Sensor.TYPE_STEP_COUNTER nicht verf�gbar");
        }
        if(mStepDetectorSensor != null){
            Log.d("Step Counter Type", "TYPE_STEP_COUNTER verfuegbar");
            mStepDetector = new StepCounter();
        }else{
            Log.d("Step Counter Type", "Auf Accelerometer Step Detection Schalten");
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mStepDetector = new StepAccelerometer();
        }
        mSensorManager.registerListener(mStepDetector, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // timer
        timerHandler.postDelayed(timerTask, 1000);

        initStepVariables();

        //handler.postDelayed(timedTask, intervalDuration);
    }

    View.OnClickListener skipSongListener = new View.OnClickListener(){
        public void onClick(View v){
            audioPlayer.skip();
        }
    };
    View.OnClickListener refreshSongListener = new View.OnClickListener(){
        public void onClick(View v){
            newPlaylist(currentBPM);
        }
    };
    View.OnClickListener pauseSongListener = new View.OnClickListener(){
        public void onClick(View v){
            audioPlayer.pause();
        }
    };
    View.OnClickListener playSongListener = new View.OnClickListener(){
        public void onClick(View v){
            audioPlayer.play();
        }
    };

    /**
     * Called by pressing the finishRun-Button. Finishes recording the track and writes it to the DB.
     * If this track is a new Track, a new ID is generated by the DB.
     * Starts the StatisticsActivity and puts the TrackID into the Intent.
     */
    public void finishRun(){
        ArrayList<Location> aTrack = mService.receiveTrack();

        int sum = 0;
        double avgBPM;
        if(BPMList.size()>0) {
            for (Integer value : BPMList) {
                sum += value;
            }
            avgBPM = sum / BPMList.size();
        }
        else {
            avgBPM = 0;
            }

        Log.d("Run Finished Listener", "Track received: " + aTrack.size() + " points");

        YaraRun mYaraRun = new YaraRun(mTrackID, avgBPM, aTrack, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));

        Log.d("Run Finished Listener", "ID: " + mTrackID +"Distance: " + mYaraRun.getRunDistance() +
                ", Duration: " + mYaraRun.getCompletionTime() +
                "s, avgSpeed: " + mYaraRun.getAvgSpeed() +
                "m/s, minSpeed: " + mYaraRun.getRunMinSpeed() +
                "m/s, maxSpeed: " + mYaraRun.getRunMaxSpeed() +
                "m/s, avgAccuracy: " + mYaraRun.getAvgAccuracy() + "m");


        // Assigning the Track ID to mYaraRun
        mYaraRun = mRunDbHelper.insertRun(mYaraRun, getBaseContext());

        mRunDbHelper.listEntries();
        mTrackDbHelper.listEntries();

        // Unload RunActivity. Code after this WILL be executed!
        finishMe();

        // If this is a run on a new Track, there is no ID yet.
        // getLastRunToTrack assigns a new ID, and returns the same mYaraRun-Object with that ID.
        mYaraRun = mRunDbHelper.getLastRunToTrack(mYaraRun.getTrackID());

        // Start StatisticsActivity
        Intent intent = new Intent(getBaseContext(), StatisticsActivity.class);
        intent.putExtra("TrackID", mYaraRun.getTrackID());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.actionFinishRun) {
            finishRun();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();

        Log.d("RunActivity onResume", "resuming RunActivity");
        mSensorManager.registerListener(mStepDetector, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

        handler.post(timedTask);

        initStepVariables();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("RunActivity", "onDestroy");

        // Unbind and stop LocationService
        unbindService(mConnection);
        stopService(locationIntent);

        // Unbind and stop AudioService
        unbindService(serviceConnection);
        stopService(audioPlayerIntent);

        //stop the timer
        timerHandler.removeCallbacks(timerTask);

        mSensorManager.unregisterListener(mStepDetector, mStepDetectorSensor);

        handler.removeCallbacks(timedTask);
        BPMList=null;
        intervalSteps = null;
        minute = false;

        // Release the WakeLock
        mWakeLock.release();
    }

    // Timertask executes every second
    private Runnable timerTask = new Runnable() {
        private int secs=0;
        private int mins=0;
        private int hours=0;

        @Override
        public void run() {
            DecimalFormat df = new DecimalFormat("#.###");
            df.setRoundingMode(RoundingMode.CEILING);
            String distance = df.format(mService.getDistanceUntilNow() / 1000);
            txtDistance.setText(distance+ " km");

            if(secs>59){
                secs =0;
                mins++;
            }
            if(mins>59){
                mins=0;
                hours++;
            }
            secs++;
            txtTime.setText((hours <10 ? "0":"")+hours+":"+(mins <10 ? "0":"")+mins+":"+(secs <10 ? "0":"")+secs);

            timerHandler.postDelayed(this, 1000);
        }
    };

    /**
     * Timed Task for tracking steps during time intervals
     *
     */
    private Runnable timedTask  = new Runnable(){
        @Override
        public void run() {
            int steps = mStepDetector.getSteps();

            if(steps != 0){

                //Last intervalFactor times steps are stored in this array to calculate the currentBPM
                intervalSteps[index % intervalFactor] = steps;
                index++;

                //after the first minute the first real BPM can be calculated
                if(minute){

                    currentBPM = calculateBPM();
                    BPMList.add(currentBPM);

                    /**
                     * normal case
                     * checks for consecutive BPM under or over your runningBPM
                     * underperforming x times results in adjustment in the music playback
                     * overperforming results in adjusting the playlist
                     */
                    if(!changeSpeed){
                        if(currentBPM < runningBPM - threshold){//under BPM
                            timesUnder++;
                            timesOver = 0;
                        }else if(currentBPM > runningBPM + threshold){//over BPM
                            timesOver++;
                            timesUnder = 0;
                        }else{//within Threshold
                            timesOver = 0;
                            timesUnder = 0;
                        }

                        if(timesUnder == timesMax){
                            //adjust music
                            float rate = 1 + (runningBPM * currentBPM/100)/100;
                            audioPlayer.adjustVolume(rate);

                            changeSpeed = true;
                        }else if (timesOver == timesMax ){
                            //select new music title to new BPM
                            timesOver = 0;
                            runningBPM = currentBPM;
                            newPlaylist(runningBPM);
                        }
                    }else{
                        /**
                         * adjust the music playback or playlist according the the logic
                         * and calculate the playback rate from currentBPM
                         */
                        if(currentBPM < runningBPM - threshold){//under BPM
                            timesUnder++;
                        }else{//within Threshold
                            timesUnder--;
                        }

                        float rate = 1+(runningBPM*currentBPM/100)/100;

                        if(timesUnder == 0){
                            //adjust music to normal
                            rate = 1.0f;
                            changeSpeed = false;
                        }else if(timesUnder == timesMax*2){
                            //select new music title to new BPM
                            rate = 1.0f;
                            changeSpeed = false;
                            timesUnder = 0;
                            runningBPM = currentBPM;
                            newPlaylist(runningBPM);
                        }

                        audioPlayer.adjustVolume(rate);
                    }
                }else{
                    currentBPM = calculateBPM();
                    if(index >= intervalFactor/3){
                        minute = true;
                        BPMList.add(currentBPM);
                    }
                }

                txtStepCountPerMinute.setText("" + currentBPM);
                Log.d("MY VERY OWN TIMED TASK", "BPM: " + currentBPM);

            }
            handler.postDelayed(timedTask, intervalDuration);
        }
    };



    private SwipeRefreshLayout.OnRefreshListener swipeRefresh = new SwipeRefreshLayout.OnRefreshListener(){
        @Override
        public void onRefresh() {
            newPlaylist(currentBPM);
            refreshLayout.setRefreshing(false);
        }
    };

    /**
     * Initialization of variables related to step detection.
     */
    private void initStepVariables() {
        runningBPM = 0;
        currentBPM = 0;
        threshold = 5;
        changeSpeed = false;

        handler = new Handler();
        intervalDuration = 5000;
        intervalFactor = 60000/intervalDuration;
        minute = false;

        timesOver = 0;
        timesUnder = 0;
        timesMax = intervalFactor/3;

        BPMList = new ArrayList<>();
        intervalSteps = new int[intervalFactor];
        index = 0;
    }

    /**
     * Calculation of step BPM
     * @return currentBPM
     */
    public int calculateBPM() {
        int bpm = 0;
        for(int i = 0; i < intervalSteps.length; i++){
            bpm += intervalSteps[i];
        }
        return bpm;
    }
}
