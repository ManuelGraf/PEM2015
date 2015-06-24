package pem.yara;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;


public class RunActivity extends ActionBarActivity {

    LocationService mService;
    Intent locationIntent;

    private TextView txtStepCount;
    private TextView txtStepCountAccelerometer;
    private TextView txtBPM;



    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepCounterAccelerometerSensor;

    private SensorEventListener mStepDetectorAccelerometer;
    private SensorEventListener mStepDetectorCounter;

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

    @Override
    protected void onStart(){
        super.onStart();
        // Bind Service
        Log.d("onStart", "Attempting to bind Service");
        locationIntent = new Intent(this, LocationService.class);
        locationIntent.putExtra("recInterval", 10000);

        Context c;
        c=this.getBaseContext();

        c.bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("onStart", "Service bound");

        c.startService(locationIntent);
        Log.d("onStart", "Service started");

//        ScanMusicTask scanMusicTask = new ScanMusicTask();
//        scanMusicTask.execute(getApplication());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        txtStepCount = (TextView)findViewById(R.id.txtStepCount);
        txtStepCountAccelerometer  = (TextView)findViewById(R.id.txtStepCountAccelerometer);
        txtBPM = (TextView)findViewById(R.id.txtBPMCount);

        //Init StepDetector
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(10);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
        textSensitive = (TextView) findViewById(R.id.textSensitive);
        textSensitive.setText(String.valueOf(10));

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();

        if(mStepCounterSensor != null){
            mSensorManager.registerListener(mStepDetectorCounter, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        //mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(mStepDetectorAccelerometer, mStepCounterAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onStop() {
        super.onStop();

        this.getBaseContext().stopService(locationIntent);
        unbindService(mConnection);

        //TODO: FLO: Wir wollen die Listener nicht abschalten, wenn die StartActivity stoppt! Sonst messen wir nur, w�hrend der User NICHT l�uft... Erledigt sich aber, wenn wir alles in die RunActivity schieben
        if(mStepCounterSensor != null){
            mSensorManager.unregisterListener(mStepDetectorCounter, mStepCounterSensor);
        }
        //mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(mStepDetectorAccelerometer, mStepCounterAccelerometerSensor);
    }

    //AB HIER ERSTMAL DER STEP DETECTOR
    //TODO: Seekbar entfernen mit den Variablen sobald unsere Sensitivitaet getestet wurde
    private float   mLimit = 10; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62

    private SeekBar seekBar;
    private TextView textSensitive;

    private void updateViewMethod(int mCount) {
        txtStepCountAccelerometer.setText("Step Counter Accelerometer : " + (mCount));
    }

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener(){

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            mLimit = seekBar.getProgress();

            textSensitive.setText(String.valueOf(mLimit));
        }

        public void onStartTrackingTouch(SeekBar seekBar){

        }

        public void onStopTrackingTouch(SeekBar seekBar){

        }
    };


    private class StepAccelerometer implements SensorEventListener {

        private final static String TAG = "StepDetector";
        //TODO: das hier wieder einkommentieren, nachdem wir die Seekbar rausgeloescht haben
        //private float   mLimit = 10; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
        private float   mLastValues[] = new float[3*2];
        private float   mScale[] = new float[2];
        private float   mYOffset;

        private float   mLastDirections[] = new float[3*2];
        private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
        private float   mLastDiff[] = new float[3*2];
        private int     mLastMatch = -1;

        private SeekBar seekBar;
        private TextView textSensitive;

        public int mCount = 0;

        public StepAccelerometer(){
            int h = 480;
            mYOffset = h * 0.5f;
            mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        }

        public void onSensorChanged(SensorEvent event){
            Sensor sensor = event.sensor;
            synchronized (this) {
                if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                }
                else {
                    int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                    if (j == 1) {
                        float vSum = 0;
                        for (int i=0 ; i<3 ; i++) {
                            final float v = mYOffset + event.values[i] * mScale[j];
                            vSum += v;
                        }
                        int k = 0;
                        float v = vSum / 3;

                        float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                        if (direction == - mLastDirections[k]) {
                            // Direction changed
                            int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                            mLastExtremes[extType][k] = mLastValues[k];
                            float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                            if (diff > mLimit) {

                                boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                                boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                                boolean isNotContra = (mLastMatch != 1 - extType);

                                if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                    Log.i(TAG, "step");
                                    mCount ++;
                                    Log.d("", "Count " + mCount);

                                    updateViewMethod(mCount);

                                    mLastMatch = extType;
                                }
                                else {
                                    mLastMatch = -1;
                                }
                            }
                            mLastDiff[k] = diff;
                        }
                        mLastDirections[k] = direction;
                        mLastValues[k] = v;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void setSensitivity(float sensitivity) {
            mLimit = sensitivity; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
        }

    }

    private class StepCounter implements SensorEventListener{

        private boolean startedStepCounter;
        private int stepCountInit;
        private long[] lastSteps;

        public StepCounter(){
            lastSteps = new long[] {0,0,0,0,0};
        }

        public void onSensorChanged(SensorEvent event){
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
                Log.d("Step detector Sensor", "da samma");
                addStep(event.timestamp);
                txtStepCount.setText("Step Counter Detected : " + (value - stepCountInit));
                txtBPM.setText("BPM: "+(int)getBPM());
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

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
    }
}
