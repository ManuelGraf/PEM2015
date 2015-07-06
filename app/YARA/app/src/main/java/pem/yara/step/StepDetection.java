package pem.yara.step;

import android.hardware.SensorEventListener;

public interface StepDetection extends SensorEventListener {

    public int getSteps();
    public int getmCount();
}
