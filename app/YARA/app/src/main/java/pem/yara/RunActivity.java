package pem.yara;

import android.app.Fragment;
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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import pem.yara.db.RunDbHelper;
import pem.yara.db.TrackDbHelper;
import pem.yara.entity.YaraRun;
import pem.yara.entity.YaraSong;
import pem.yara.fragments.SongListFragment;
import pem.yara.music.AudioPlayer;
import pem.yara.step.StepAccelerometer;
import pem.yara.step.StepCounter;
import pem.yara.step.StepDetection;


public class RunActivity extends ActionBarActivity implements SongListFragment.OnSongListInteractionListener {

    private int mTrackID;
    private PowerManager.WakeLock mWakeLock;

    LocationService mService;
    Intent locationIntent;

    private ServiceConnection serviceConnection = new AudioPlayerServiceConnection();
    private AudioPlayer audioPlayer;
    private Intent audioPlayerIntent;

    private Fragment songlistFragment;

    private TextView    txtStepCount;
    private TextView    txtStepCountAccelerometer;

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
            Log.v("StartActivity", "AudioPlayerServiceConnection: Service connected");
            audioPlayer = ((AudioPlayer.AudioPlayerBinder) baBinder).getService();
            startService(audioPlayerIntent);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("StartActivity", "AudioPlayerServiceConnection: Service disconnected");
            audioPlayer = null;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        // Bind Service
        Log.d("onStart", "Attempting to bind Service");
        locationIntent = new Intent(this, LocationService.class);

        Context c;
        c=this.getBaseContext();

        try {
            mTrackID = getIntent().getExtras().getInt("TrackID");

            YaraRun mYaraRun = mRunDbHelper.getLastRunToTrack(mTrackID);
            double lastBPM;

            if(mYaraRun==null){
                lastBPM = 50;   // Startwert
            } else {
                lastBPM = mYaraRun.getAvgBpm();
            }


            audioPlayer.adjustPlaylist(lastBPM);

            Log.d("Statistics onCreate", "TrackID: " + mTrackID);
        } catch (Exception e){
            e.printStackTrace();
            Log.d("Statistics onCreate", "No TrackID passed, new track!");
            mTrackID = -1;
        }

        // Bind service to later be able to address the mService-Object to get a recorded Track
        c.startService(locationIntent);
        c.bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("RunActivity onStart", "LocationService bound");

        audioPlayerIntent = new Intent(this, AudioPlayer.class);
        startService(audioPlayerIntent);
        bindService(audioPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d("RunActivity onStart", "AudioService bound");
    }

    private void finishMe(){
        this.finish();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        txtStepCount = (TextView)findViewById(R.id.txtStepCount);
        txtStepCountAccelerometer  = (TextView)findViewById(R.id.txtStepCountAccelerometer);

        txtStepCountPerMinute = (TextView)findViewById(R.id.txtStepCountPerMinute);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance =(TextView)findViewById(R.id.txtTime);

        // Acquire a partial WakeLock, to keep the background services running even if the phone goes to sleep
        PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        mWakeLock.acquire();

        mRunDbHelper = new RunDbHelper(getBaseContext());
        mTrackDbHelper = new TrackDbHelper(getBaseContext());

        //Init StepDetector
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
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

        //TODO: Why are we doing this here?!?
        mRunDbHelper.getLastRunToTrack(mYaraRun.getTrackID());

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
        }else if(id == R.id.actionSkipSong){
            audioPlayer.skip();

        }else if(id == R.id.actionRefreshPlaylist){
            // @TODO get a more suitable playlist for current bpm

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
            if(secs==60){
                secs =0;
                mins++;
            }
            if(mins==60){
                mins=0;
                hours++;
            }
            secs++;
            txtTime.setText((hours <10 ? "0":"")+hours+":"+(mins <10 ? "0":"")+mins+":"+(secs <10 ? "0":"")+secs);

            //Update von den Schrittfeldern
            txtStepCount.setText("Step Counter Accelerometer : " + (mStepDetector.getmCount()));

            timerHandler.postDelayed(this, 1000);
        }
    };


    private Runnable timedTask  = new Runnable(){
        @Override
        public void run() {
            Log.d("MY VERY OWN TIMED TASK", "BPM: " + currentBPM);
            int steps = mStepDetector.getSteps();

            if(steps != 0){

                intervalSteps[index % intervalFactor] = steps;
                index++;

                if(minute){

                    currentBPM = calculateBPM();
                    BPMList.add(currentBPM);

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
                            audioPlayer.adjustRate(rate);

                            changeSpeed = true;
                        }else if (timesOver == timesMax*2 ){
                            //select new music title to new BPM
                            timesOver = 0;
                            runningBPM = currentBPM;
                            audioPlayer.adjustPlaylist(runningBPM);
                        }
                    }else{
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
                            audioPlayer.adjustPlaylist(runningBPM);
                        }

                        audioPlayer.adjustRate(rate);
                    }
                }else{
                    currentBPM = calculateBPM();
                    if(index >= intervalFactor){
                        minute = true;
                        BPMList.add(currentBPM);
                    }
                }

                txtStepCountPerMinute.setText(""+currentBPM);
                txtStepCount.setText("Step Counter: " + (mStepDetector.getmCount()));

            }
            handler.postDelayed(timedTask, intervalDuration);
        }
    };

    @Override
    public void onSongListInteraction(YaraSong s) {

    }

    @Override
    public void onImportMusicInteraction() {

    }

    private void initStepVariables() {
        runningBPM = 0;
        currentBPM = 0;
        threshold = 5;
        changeSpeed = false;

        handler = new Handler();
        intervalDuration = 10000;
        intervalFactor = 60000/intervalDuration;
        minute = false;

        timesOver = 0;
        timesUnder = 0;
        timesMax = intervalFactor/2;

        BPMList = new ArrayList<>();
        intervalSteps = new int[intervalFactor];
        index = 0;
    }

    public int calculateBPM() {
        int bpm = 0;
        for(int i = 0; i < intervalSteps.length; i++){
            bpm += intervalSteps[i];
        }
        return bpm;
    }
}
