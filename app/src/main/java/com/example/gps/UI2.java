package com.example.gps;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
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
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    SensorManager pressureSensorManager;
    SensorManager temperatureSensorManager;

    boolean isEnabledGPS;
    boolean isEnabledNet;
    TextView gps;
    private LocationManager locationManager;

    final MyTimer myTimer = new MyTimer();

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

    private static final int   SENT     = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onCreate",""+hasPermissions(this,PERMISSIONS));
        setContentView(R.layout.activity_ui2);
        firstInit();
    }

    private void firstInit()
    {
        message = new Message();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasPermissions(this,PERMISSIONS))requestPermissions();
        }
        Button btnSendMessage = findViewById(R.id.btn_send_message);
        Button btnSendDataMessage = findViewById(R.id.btn_send_data_message);
        final TextView textView = findViewById(R.id.message);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Message ",message.getSql());
                textView.setText(message.getSql());
                sendSMS();
            }
        });

        btnSendDataMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Message ",message.getSql());
                textView.setText(message.getSql());
                sendDataSms();
            }
        });



        message.name = getDeviceId();
        myCompassView = findViewById(R.id.mycompassview);

        gps = findViewById(R.id.gps);
//change
        gps.setText("Получаю координаты");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        initSensors();
    }

    private void sendSMS()
    {
        PendingIntent sent = this.createPendingResult(SENT, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        String messageText = message.getSql();
        String phoneNumber = "+380504205770";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);
    }

    //https://stackoverflow.com/questions/3757229/how-to-send-and-receive-data-sms-messages
    //http://wiebe-elsinga.com/blog/sending-and-receiving-data-sms-messages-with-android/
    //https://codetheory.in/android-sms/
    private void sendDataSms()
    {
        short SMS_PORT = 8901;
        PendingIntent sent = this.createPendingResult(SENT, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        String messageText = message.getSql();
        String phoneNumber = "+380504205770";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendDataMessage(phoneNumber, null, SMS_PORT, messageText.getBytes(), sent, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String toast = null;
        switch (requestCode) {
            case SENT:
                switch (resultCode) {
                    case RESULT_OK:
                        toast = "OK";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        toast = "Generic Failure";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        toast = "Radio Off";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        toast = "Null Pdu";
                        break;
                }
                break;
        }
        if (toast != null) {
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }

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
                message.azimuth = ""+(int)event.values[0];
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
            mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);;

        } else {
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
        startTransferData();
    }

    private void startTransferData()
    {
        if(hasPermissions(this,PERMISSIONS))
        {
            if(myTimer.cancelled == true)
            {
                myTimer.myTimer = new Timer();
                myTimer.cancelled = false;
            }

            myTimer.myTimer.schedule(new TimerTask() { // Определяем задачу
                @Override
                public void run() {
                    Log.e("message onResume",message.type+" "+message.getSql());
                    if(message.type!=0)
                    {
                        new Api().execute("12",message.getSql());
                    }
                };
            }, 1000L, 5L * 1000);


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
        endTransferData();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void endTransferData()
    {
        if(hasPermissions(this,PERMISSIONS)) {
            locationManager.removeUpdates(locationListener);
            myTimer.myTimer.cancel();
            myTimer.cancelled = true;
        }
    }


    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
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
            message.latitude=""+location.getLatitude();
            message.longitude=""+location.getLongitude();
            message.altitude=""+location.getAltitude();
            message.type=1;
            Log.e("GPS AZIMUTH", ""+location.getBearing());
        }
        else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            if (!isEnabledGPS)
            {
                gps.setText(formatLocation(location));
                message.latitude=""+location.getLatitude();
                message.longitude=""+location.getLongitude();
                message.altitude=""+location.getAltitude();
                location.getAccuracy();
                message.type=2;
            }
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";

        return String.format(
                "шир = %1$.5f\nдол = %2$.5f",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }


    public void onClickLocationSettings(View view) {
        if(hasPermissions(this,PERMISSIONS)) startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        else
            requestPermissions();
    }


}
