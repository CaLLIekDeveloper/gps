package com.example.gps;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
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

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UI extends Activity {
    private static SensorManager mySensorManager;
    private boolean sersorrunning;
    private MyCompassView myCompassView;

    private Compass mCompass;
    Message message;
    SensorManager pressureSensorManager;
    SensorManager temperatureSensorManager;



    final java.util.Timer myTimer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui);
        myCompassView = findViewById(R.id.mycompassview);
        message = new Message();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            message.name = getDeviceId();
        }
        else
            message.name = "unknownId";

        Log.e("message",message.getSql());

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

        pressureSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor defPressureSensor = pressureSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        pressureSensorManager.registerListener(pressureSensorEventListener, defPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);

       temperatureSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor temperatureSensor = pressureSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressureSensorManager.registerListener(temperatureSensorEventListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);


        if (mySensors.size() > 0) {
            mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            sersorrunning = true;
            Toast.makeText(this, "Start ORIENTATION Sensor", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "No ORIENTATION Sensor", Toast.LENGTH_LONG).show();
            sersorrunning = false;
            finish();
        }


        gps = findViewById(R.id.gps);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        TextView textView = findViewById(R.id.angle);

        mCompass = new Compass(this, textView);

        mCompass.averageAngle();


        //new Api().execute("12",message.getSql());
        // Создаем таймер


        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                Log.e("message",message.getSql());
                long answer = (long)1;
                if(message.type!=0)
                {
                    new Api().execute("12",message.getSql());
                }
                if(answer== -1)
                {
                    myTimer.cancel();
                }
            };
        }, 1000L, 5L * 1000); // интервал - 20000 миллисекунд,  миллисекунд до первого запуска.

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getDeviceId() {
        ActivityCompat.checkSelfPermission(UI.this, Manifest.permission.READ_SMS);
        ActivityCompat.checkSelfPermission(UI.this, Manifest.permission.READ_PHONE_NUMBERS);
        ActivityCompat.checkSelfPermission(UI.this, Manifest.permission.READ_PHONE_STATE);
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to requestPermissions the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return "Need Permission";
        }

        return ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getLine1Number();
    }

    private final SensorEventListener pressureSensorEventListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            // Получаем атмосферное давление в миллибарах
            double pressure = event.values[0];
            message.pressure = ""+pressure;
        }
    };

    private final SensorEventListener temperatureSensorEventListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            // Получаем атмосферное давление в миллибарах
            double temperature = event.values[0];
            message.temperature = ""+temperature;
        }
    };

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            myCompassView.updateDirection((float) event.values[0]);

            float millibarsOfPressure = event.values[0];

            TextView tv = findViewById(R.id.height2);

            tv.setText(""+SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,millibarsOfPressure));

        }
    };

    @Override
    protected void onDestroy() {
// TODO Auto-generated method stub
        super.onDestroy();

        if (sersorrunning) {
            mySensorManager.unregisterListener(mySensorEventListener);
        }

        pressureSensorManager.unregisterListener(pressureSensorEventListener);
        temperatureSensorManager.unregisterListener(temperatureSensorEventListener);

    }


    boolean isEnabledGPS;
    boolean isEnabledNet;
    TextView gps;

    private LocationManager locationManager;

    @Override
    protected void onResume() {
        super.onResume();
        mCompass.registerListener(SensorManager.SENSOR_DELAY_NORMAL);
        if (hasPermissions(this, PERMISSIONS)) {
            gps.setText("Получаю координаты");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                request();
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to requestPermissions the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                if (!hasPermissions(this, PERMISSIONS)) return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);

    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(locationListener);
        mCompass.unregisterListener();
    }

    public void request() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, 124);
    }


    //получаем разрешения приложения
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_SMS,Manifest.permission.READ_PHONE_STATE};


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
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to requestPermissions the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
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

        TextView tv = findViewById(R.id.angle);

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            gps.setText(formatLocation(location));
            isEnabledGPS = true;
            Log.e("GPS", "status: " + formatLocation(location));

            tv.setText("Угол " + location.getBearing());
            message.latitude=""+location.getLatitude();
            message.longitude=""+location.getLongitude();
            message.altitude=""+location.getAltitude();
            message.type=1;
            message.azimuth = ""+location.getBearing();
        } else if (location.getProvider().equals(

                LocationManager.NETWORK_PROVIDER)) {
            if (!isEnabledGPS)
            {
                gps.setText(formatLocation(location));

                message.latitude=""+location.getLatitude();
                message.longitude=""+location.getLongitude();
                message.altitude=""+location.getAltitude();
                message.type=2;
                message.azimuth = ""+location.getBearing();
            }
            //tvLocationNet.setText(formatLocation(location));
            tv.setText("Угол " + location.getBearing());
        }
        Double at = location.getAltitude();
        TextView tvElevation = findViewById(R.id.height);
        tvElevation.setText("Location Attitude " + location.getAltitude());

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
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
