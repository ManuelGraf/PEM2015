package pem.yara.step;

import android.hardware.SensorEventListener;
/**
 * Interface for implemented StepCounter
 */
public interface StepDetection extends SensorEventListener {

    public int getSteps();
    public int getmCount();
}
