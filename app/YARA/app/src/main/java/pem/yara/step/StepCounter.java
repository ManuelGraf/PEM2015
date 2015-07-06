package pem.yara.step;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
/**
 * StepCounter via Android Sensor.TYPE_STEP_COUNTER
 */
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
     * EventListener for Step_Counter Events
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

    /**
     * Returns step count since the last call
     * @return step count
     */
    public int getSteps(){
        int difference = mCount-mCountLast;
        mCountLast = mCount;
        return difference;
    }

    /**
     * Getter for mCount variable. Used for Display
     * @return mCount
     */
    public int getmCount() {
        return mCount;
    }
}
