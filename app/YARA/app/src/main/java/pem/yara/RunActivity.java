package pem.yara;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.os.IBinder;
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
import pem.yara.music.AudioPlayer;


public class RunActivity extends ActionBarActivity {

    private int mTrackID;

    LocationService mService;
    Intent locationIntent;

    private ServiceConnection serviceConnection = new AudioPlayerServiceConnection();
    private AudioPlayer audioPlayer;
    private Intent audioPlayerIntent;

    private TextView    txtStepCount;
    private TextView    txtStepCountAccelerometer;

    // run overview part obendrueber
    private Handler timerHandler = new Handler();

    private TextView    txtStepCountPerMinute;
    private TextView    txtTime;
    private TextView    txtDistance;


    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepCounterAccelerometerSensor;

    private StepAccelerometer mStepDetectorAccelerometer;
    private StepCounter mStepDetectorCounter;

    private Handler handler;
    //in ms
    private int intervalDuration;

    private int runningBPM;
    private int currentBPM;
    private int threshold;

    private int timesOver;
    private int timesUnder;
    private int timesMax;
    private boolean changeSpeed;



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

            Log.d("Statistics onCreate", "TrackID: " + mTrackID);
        } catch (Exception e){
            Log.d("Statistics onCreate", "No TrackID passed, new track!");
            return;
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

        //Init StepDetector
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorCounter = new StepCounter();
/*        mStepDetectorSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);*/
        mStepCounterAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mStepDetectorAccelerometer = new StepAccelerometer();

        mSensorManager.registerListener(mStepDetectorAccelerometer, mStepCounterAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        if(mStepCounterSensor != null){
            Log.d("Step Counter Type", "TYPE_STEP_COUNTER verfuegbar");
            mSensorManager.registerListener(mStepDetectorCounter, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }else{
            Log.d("Step Counter Type", "Auf Accelerometer Step Detection Schalten");
            //TODO: Nach dem Testen das hier einkommentieren und anpassen, dass immer der verfuegbare Sensor verwendet wird
            //mSensorManager.registerListener(mStepDetectorAccelerometer, mStepCounterAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        // timer
        timerHandler.postDelayed(timerTask, 1000);

        runningBPM = 0;
        currentBPM = 0;
        threshold = 5;
        changeSpeed = false;

        handler = new Handler();
        intervalDuration = 10000;



        //handler.postDelayed(timedTask, intervalDuration);
    }
    public void finishRun(){
        ArrayList<Location> aTrack = mService.receiveTrack();

        Log.d("Run Finished Listener", "Track received: " + aTrack.size() + " points");
        // TODO: Wenn dies ein bekannter Track ist, muss hier irgendwo die TrackID zu finden sein!

        YaraRun mYaraRun = new YaraRun(mTrackID, 9001, aTrack, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));

        Log.d("Run Finished Listener", "ID: " + mTrackID +"Distance: " + mYaraRun.getRunDistance() +
                ", Duration: " + mYaraRun.getCompletionTime() +
                "s, avgSpeed: " + mYaraRun.getAvgSpeed() +
                "m/s, minSpeed: " + mYaraRun.getRunMinSpeed() +
                "m/s, maxSpeed: " + mYaraRun.getRunMaxSpeed() +
                "m/s, avgAccuracy: " + mYaraRun.getAvgAccuracy() + "m");

        RunDbHelper mRunDbHelper = new RunDbHelper(getBaseContext());
        TrackDbHelper mTrackDbHelper = new TrackDbHelper(getBaseContext());

        // Assigning the Track ID to mYaraRun
        mYaraRun = mRunDbHelper.insertRun(mYaraRun, getBaseContext());

        mRunDbHelper.listEntries();
        mTrackDbHelper.listEntries();

        // Unload RunActivity. Code after this WILL be executed!
        finishMe();

        mRunDbHelper.getLastRunToTrack(mYaraRun.getTrackID());

        // TODO Get BPM from this run!
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
            // @TODO Skip to the next Song

        }else if(id == R.id.actionRefreshPlaylist){
            // @TODO get a more suitable playlist for current bpm

        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();

        Log.d("RunActivity onResume", "resuming RunActivity");

        if(mStepCounterSensor != null){
            mSensorManager.registerListener(mStepDetectorCounter, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        //mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mStepDetectorAccelerometer, mStepCounterAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        handler.post(timedTask);

    }

    protected void onStop() {
        super.onStop();

        // Unbind and stop LocationService
        unbindService(mConnection);
        stopService(locationIntent);

        // Unbind and stop AudioService
        unbindService(serviceConnection);
        stopService(audioPlayerIntent);

        //stop the timer
        timerHandler.removeCallbacks(timerTask);

        if(mStepCounterSensor != null){
            mSensorManager.unregisterListener(mStepDetectorCounter, mStepCounterSensor);
        }
        //mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(mStepDetectorAccelerometer, mStepCounterAccelerometerSensor);

        handler.removeCallbacks(timedTask);
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
            txtStepCountAccelerometer.setText("Step Counter Accelerometer : " + (mStepDetectorAccelerometer.mCount));
            if(mStepCounterSensor != null){
                txtStepCount.setText("Step Counter StepDetector : " + (mStepDetectorCounter.mCount));
            }

            timerHandler.postDelayed(this, 1000);
        }
    };


    private Runnable timedTask  = new Runnable(){
        @Override
        public void run() {
            Log.d("MY VERY OWN TIMED TASK", "BPM: "+currentBPM);
            int steps = mStepDetectorAccelerometer.getSteps();
            if(steps != 0){
                currentBPM = steps*(60000/intervalDuration);

                txtStepCountPerMinute.setText(""+currentBPM);
                txtStepCountAccelerometer.setText("Step Counter Accelerometer : " + (mStepDetectorAccelerometer.mCount));
                if(mStepCounterSensor != null){
                    txtStepCount.setText("Step Counter StepDetector : " + (mStepDetectorCounter.mCount));
                }

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
                        changeSpeed = true;
                    }else if (timesOver == timesMax*2 ){
                        //select new music title to new BPM
                        runningBPM = currentBPM;
                    }
                }else{
                    if(currentBPM < runningBPM - threshold){//under BPM
                        timesUnder++;
                    }else{//within Threshold
                        timesUnder--;
                    }

                    if(timesUnder == 0){
                        //adjust music to normal
                        changeSpeed = false;
                    }else if(timesUnder == timesMax*2){
                        //select new music title to new BPM
                        runningBPM = currentBPM;
                    }
                }
            }
            handler.postDelayed(timedTask, intervalDuration);
        }
    };
}
