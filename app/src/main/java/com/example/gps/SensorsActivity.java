package com.example.gps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class SensorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        TextView sens = findViewById(R.id.sensors);
        sens.setText("Количество датчиков "+sensors.size()+"\n");
        for(int i=0; i<sensors.size(); i++)
        {
            sens.setText(sens.getText()+sensors.get(i).getName()+"\n");
        }
    }
}
