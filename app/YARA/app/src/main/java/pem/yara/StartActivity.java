package pem.yara;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import pem.yara.LocalService.LocalBinder;


public class StartActivity extends ActionBarActivity implements SensorEventListener  {

    LocalService mService;
    boolean mBound = false;

    private TextView txtStepCount;
    private TextView txtBPM;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;

    private Button btnShowStats;
    private Button btnStartRun;
    private Button btnNewRun;
    private Button btnShowSongs;

    private boolean startedStepCounter;
    private int stepCountInit;
    private long[] lastSteps;

    public void addStep(long timestamp){
        for (int i=0; i< lastSteps.length; i++){
            if( i+1 < lastSteps.length && lastSteps[i+1] != 0){
                lastSteps[i] = lastSteps[i+1];
            }
        }
        lastSteps[lastSteps.length-1] = timestamp;
    }
    public double getBPM(){
        // calculate average bpm from the last few steps.
        long startTime = lastSteps[0];
        long lastTime = lastSteps[lastSteps.length-1];
        double delta = (lastTime - startTime); // time for 5 steps
        double bpm = (lastSteps.length * (60/(delta/1000000000)));

        if(startTime != 0 && lastTime != 0){
            return bpm;
        }else{
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        txtStepCount = (TextView)findViewById(R.id.txtStepCount);
        txtBPM = (TextView)findViewById(R.id.txtBPMCount);

        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        /*mStepDetectorSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
*/
        lastSteps = new long[] {0,0,0,0,0};

        btnStartRun = (Button)findViewById(R.id.btnStartRunning);
        btnStartRun.setOnClickListener(startRunListener);
        btnNewRun = (Button)findViewById(R.id.btnRegisterTrack);
        btnNewRun.setOnClickListener(newTrackListener);
        btnShowStats = (Button)findViewById(R.id.btnShowStatistics);
        btnShowStats.setOnClickListener(showStatisticsListener);
        btnShowSongs = (Button)findViewById(R.id.btnShowSongList);
        btnShowSongs.setOnClickListener(showSonglistListener);

    }

    @Override
    protected void onStart(){
        super.onStart();
        // Bind Service
        Log.d("onStart", "Attempting to bind Service");
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("onStart", "Attempt over... Service bound? " + mBound);
    }

    protected void onResume() {

        super.onResume();

        mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, LocalService.class));
//        if(mBound) {
            unbindService(mConnection);
            mBound = false;
//        }
        mSensorManager.unregisterListener(this, mStepCounterSensor);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("onServiceDisconnected", "onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    public void onAccuracyChanged(Sensor s, int i){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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

    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
            if(!startedStepCounter){
                startedStepCounter = true;
                stepCountInit = value;
            }
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            addStep(event.timestamp);
            txtStepCount.setText("Step Counter Detected : " + (value - stepCountInit));
            txtBPM.setText("BPM: "+(int)getBPM());
        }
    }

    View.OnClickListener showStatisticsListener = new View.OnClickListener(){
      public void onClick(View v){
          Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
          startActivity(intent);
      }
    };
    View.OnClickListener startRunListener = new View.OnClickListener(){
      public void onClick(View v){
          Intent intent = new Intent(getApplicationContext(), RunActivity.class);
          startActivity(intent);
      }
    };
    View.OnClickListener newTrackListener = new View.OnClickListener(){
        public void onClick(View v){
            Intent intent = new Intent(getApplicationContext(), RunActivity.class);
            startActivity(intent);
        }
    };
    View.OnClickListener showSonglistListener = new View.OnClickListener(){
      public void onClick(View v){
         /* Intent intent = new Intent(getApplicationContext(), StartActivity.class);
          startActivity(intent);*/
      }
    };
}
