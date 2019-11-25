package com.example.gps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import java.util.LinkedList;

public class Compass  implements SensorEventListener
{
    public static final float TWENTY_FIVE_DEGREE_IN_RADIAN = 0.436332313f;
    public static final float ONE_FIFTY_FIVE_DEGREE_IN_RADIAN = 2.7052603f;

    private SensorManager mSensorManager;
    private float[] mGravity;
    private float[] mMagnetic;
    // If the device is flat mOrientation[0] = azimuth, mOrientation[1] = pitch
    // and mOrientation[2] = roll, otherwise mOrientation[0] is equal to Float.NAN
    private float[] mOrientation = new float[3];
    private LinkedList<Float> mCompassHist = new LinkedList<>();
    private float[] mCompassHistSum = new float[]{0.0f, 0.0f};
    private int mHistoryMaxLength;

    private TextView angle;
    public Compass(Context context,final TextView textView)
    {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // Adjust the history length to fit your need, the faster the sensor rate
        // the larger value is needed for stable result.
        mHistoryMaxLength = 20;
        angle = textView;
    }

    public void registerListener(int sensorRate)
    {
        Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticSensor != null)
        {
            mSensorManager.registerListener(this, magneticSensor, sensorRate);
        }
        Sensor gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (gravitySensor != null)
        {
            mSensorManager.registerListener(this, gravitySensor, sensorRate);
        }
    }

    public void unregisterListener()
    {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
        {
            mGravity = event.values.clone();
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mMagnetic = event.values.clone();
        }
        if (!(mGravity == null || mMagnetic == null))
        {
            mOrientation = getOrientation();
        }

        angle.setText(""+averageAngle());
    }

    private float[] getOrientation()
    {
        float[] rotMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotMatrix, null,
                mGravity, mMagnetic))
        {
            float inclination = (float) Math.acos(rotMatrix[8]);
            // device is flat
            if (inclination < TWENTY_FIVE_DEGREE_IN_RADIAN
                    || inclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN)
            {
                float[] orientation = mSensorManager.getOrientation(rotMatrix, mOrientation);
                mCompassHist.add(orientation[0]);
                mOrientation[0] = averageAngle();
            }
            else
            {
                mOrientation[0] = Float.NaN;
                clearCompassHist();
            }
        }
        return rotMatrix;
    }


    private void clearCompassHist()
    {
        mCompassHistSum[0] = 0;
        mCompassHistSum[1] = 0;
        mCompassHist.clear();
    }

    public float averageAngle()
    {
        int totalTerms = mCompassHist.size();
        //Log.e("size", ""+totalTerms);
        if (totalTerms > mHistoryMaxLength)
        {
            float firstTerm = mCompassHist.removeFirst();
            mCompassHistSum[0] -= Math.sin(firstTerm);
            mCompassHistSum[1] -= Math.cos(firstTerm);
            totalTerms -= 1;
        }
        if(mCompassHist.size()>0) {
            float lastTerm = mCompassHist.getLast();
            mCompassHistSum[0] += Math.sin(lastTerm);
            mCompassHistSum[1] += Math.cos(lastTerm);
            float angle = (float) Math.atan2(mCompassHistSum[0] / totalTerms, mCompassHistSum[1] / totalTerms);
            return angle;
        }
        else
            return 3f;
    }
}