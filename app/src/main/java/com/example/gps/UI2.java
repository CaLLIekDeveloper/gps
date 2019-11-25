package com.example.gps;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UI2 extends AppCompatActivity {

    private MyCompassView myCompassView;
    Message message;
    Message message2;
    SensorManager pressureSensorManager;
    SensorManager temperatureSensorManager;

    boolean isEnabledGPS;
    boolean isEnabledNet;
    TextView gps;
    private LocationManager locationManager;

    final java.util.Timer myTimer = new Timer();

    public void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                //если разрешение не получено приложением вернуть false
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("create","create");
        Log.e("onCreate",""+hasPermissions(this,PERMISSIONS));
        setContentView(R.layout.activity_ui2);
        message = new Message();
        message2 = new Message();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasPermissions(this,PERMISSIONS))requestPermissions();
        }
    }

    private void firstInit()
    {
        message.name = getDeviceId();
        message2.name = message.name;
        myCompassView = findViewById(R.id.mycompassview);

        gps = findViewById(R.id.gps);
//change
        gps.setText("Получаю координаты");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        initSensors();
    }

    private void initSensors()
    {
        initPressureSensor();
        initTemperatureSensor();
        initCompassSensor();
    }

    private List<float[]> mRotHist = new ArrayList<float[]>();
    private int mRotHistIndex;
    // Change the value so that the azimuth is stable and fit your requirement
    private int mHistoryMaxLength = 40;
    float[] mGravity;
    float[] mMagnetic;
    float[] mRotationMatrix = new float[9];
    // the direction of the back camera, only valid if the device is tilted up by
// at least 25 degrees.
    private float mFacing = Float.NaN;

    public static final float TWENTY_FIVE_DEGREE_IN_RADIAN = 0.436332313f;
    public static final float ONE_FIFTY_FIVE_DEGREE_IN_RADIAN = 2.7052603f;

    private void initCompassSensor()
    {
        SensorManager mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

        if(mySensors.size()==0)return;

        SensorEventListener mySensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onSensorChanged(SensorEvent event) {
                // TODO Auto-generated method stub
                myCompassView.updateDirection((float) event.values[0]);

                if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
                {
                    mGravity = event.values.clone();
                }
                else
                {
                    mMagnetic = event.values.clone();
                }

                if (mGravity != null && mMagnetic != null)
                {
                    if (SensorManager.getRotationMatrix(mRotationMatrix, null, mGravity, mMagnetic))
                    {
                        // inclination is the degree of tilt by the device independent of orientation (portrait or landscape)
                        // if less than 25 or more than 155 degrees the device is considered lying flat
                        float inclination = (float) Math.acos(mRotationMatrix[8]);
                        if (inclination < TWENTY_FIVE_DEGREE_IN_RADIAN
                                || inclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN)
                        {
                            // mFacing is undefined, so we need to clear the history
                            clearRotHist();
                            mFacing = Float.NaN;
                        }
                        else
                        {
                            setRotHist();
                            // mFacing = azimuth is in radian
                            mFacing = findFacing();
                        }
                    }
                }

                float millibarsOfPressure = event.values[0];
                //TextView tv = findViewById(R.id.height2);
                //tv.setText(""+SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,millibarsOfPressure));
            }
        };

        if (mySensors.size() > 0) {
            mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(this, "Start ORIENTATION Sensor", Toast.LENGTH_LONG).show();

        } else {

           //Toast.makeText(this, "No ORIENTATION Sensor", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void clearRotHist()
    {
        mRotHist.clear();
        mRotHistIndex = 0;
    }

    private void setRotHist()
    {
        float[] hist = mRotationMatrix.clone();
        if (mRotHist.size() == mHistoryMaxLength)
        {
            mRotHist.remove(mRotHistIndex);
        }
        mRotHist.add(mRotHistIndex++, hist);
        mRotHistIndex %= mHistoryMaxLength;
    }

    private float findFacing()
    {
        float[] averageRotHist = average(mRotHist);
        return (float) Math.atan2(-averageRotHist[2], -averageRotHist[5]);
    }

    public float[] average(List<float[]> values)
    {
        float[] result = new float[9];
        for (float[] value : values)
        {
            for (int i = 0; i < 9; i++)
            {
                result[i] += value[i];
            }
        }

        for (int i = 0; i < 9; i++)
        {
            result[i] = result[i] / values.size();
        }

        return result;
    }

    private void initPressureSensor()
    {
        pressureSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor defPressureSensor = pressureSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(defPressureSensor==null)return;;

       final SensorEventListener pressureSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                // Получаем атмосферное давление в миллибарах
                double pressure = event.values[0];
                message.pressure = ""+pressure;
            }
        };
        pressureSensorManager.registerListener(pressureSensorEventListener, defPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initTemperatureSensor()
    {
        temperatureSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor temperatureSensor = pressureSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if(temperatureSensor==null)
            temperatureSensor = pressureSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

        if(temperatureSensor==null)return;

        final SensorEventListener temperatureSensorEventListener = new SensorEventListener() {

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                double temperature = event.values[0];
                message.temperature = ""+temperature;
            }
        };

        pressureSensorManager.registerListener(temperatureSensorEventListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume",""+hasPermissions(this,PERMISSIONS));
        if(hasPermissions(this,PERMISSIONS))
        {
            firstInit();
            myTimer.schedule(new TimerTask() { // Определяем задачу
                @Override
                public void run() {
                    Log.e("message onResume",message.getSql());
                    long answer = (long)1;
                    if(message.type!=0)
                    {
                        new Api().execute("12",message.getSql());
                    }
                    if(message2.type!=0)
                    {
                        new Api().execute("12",message2.getSql());
                    }
                    if(answer== -1)
                    {
                        myTimer.cancel();
                    }
                };
            }, 1000L, 5L * 1000); // интервал - 20000 миллисекунд,  миллисекунд до первого запуска.


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000 * 10, 10, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                    locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onPause",""+hasPermissions(this,PERMISSIONS));

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop",""+hasPermissions(this,PERMISSIONS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("onDestroy",""+hasPermissions(this,PERMISSIONS));

        if(hasPermissions(this,PERMISSIONS)) {
            locationManager.removeUpdates(locationListener);
            myTimer.cancel();
        }
    }


    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public String getDeviceId() {
        return "35" +
                Build.BOARD.length()%10 + Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10;
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onProviderEnabled(String provider) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                if (status == 0) {
                    isEnabledGPS = false;
                } else
                    isEnabledGPS = true;

                Log.e("gps", "" + status);
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {

                if (status == 0) {
                    isEnabledNet = false;
                } else
                    isEnabledNet = true;

                Log.e("net", "" + status);
            }
        }
    };


    private void showLocation(Location location) {

        if (location == null)
            return;


        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            gps.setText(formatLocation(location));
            isEnabledGPS = true;
            Log.e("GPS", "status: " + formatLocation(location));


            message.latitude=""+location.getLatitude();
            message.longitude=""+location.getLongitude();
            message.altitude=""+location.getAltitude();
            message.type=1;
            message.azimuth = ""+myCompassView.direction;
        } else if (location.getProvider().equals(

                LocationManager.NETWORK_PROVIDER)) {
            if (!isEnabledGPS)
            {
                gps.setText(formatLocation(location));
                message2.latitude=""+location.getLatitude();
                message2.longitude=""+location.getLongitude();
                message2.altitude=""+location.getAltitude();
                location.getAccuracy();
                message2.type=2;
                message2.azimuth = ""+myCompassView.direction;
            }
            //tvLocationNet.setText(formatLocation(location));
        }
        Double at = location.getAltitude();
        Log.e("at",""+at);
        Log.e("direction",""+myCompassView.direction);

    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";

        return String.format(
                "Координаты: ширина = %1$.4f, долгота = %2$.4f",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }


    public void onClickLocationSettings(View view) {
        if(hasPermissions(this,PERMISSIONS)) startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        else
            requestPermissions();
    }
}
