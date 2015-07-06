package pem.yara.step;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class StepCounter implements StepDetection {

    private boolean startedStepCounter;
    private int stepCountInit;

    private int mCount;
    private int mCountLast;

    public StepCounter(){
        mCount = 0;
        mCountLast = 0;
    }

    /**
     * Finds a view that was identified by the id attribute from the XML that
     * was processed in.
     *
     * @return The view if found or null otherwise.
     */
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
            mCount = value - stepCountInit;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public int getSteps(){
        int difference = mCount-mCountLast;
        mCountLast = mCount;
        return difference;
    }


    public int getmCount() {
        return mCount;
    }
}
