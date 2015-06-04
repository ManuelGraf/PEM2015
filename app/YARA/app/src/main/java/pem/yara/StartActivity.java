package pem.yara;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class StartActivity extends ActionBarActivity implements SensorEventListener  {

    private TextView txtStepCount;
    private TextView txtBPM;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;

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
    }


    protected void onResume() {

        super.onResume();

        mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mStepCounterSensor);
    }

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
}
