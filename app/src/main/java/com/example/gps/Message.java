package com.example.gps;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    //name,type,latitude,longitude,azimuth,altitude,temperature,pressure
    String name = "";
    int type = 0;
    String latitude= "";
    String longitude= "";
    String azimuth= "";
    String altitude= "";
    String temperature= "";
    String pressure= "";

    public String getSql()
    {
        // Текущее время
        Date currentDate = new Date();
        // Форматирование времени как "день.месяц.год"
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        return name+"|"+dateText+"|"+type+"|"+latitude+"|"+longitude+"|"+azimuth+"|"+altitude+"|"+temperature+"|"+pressure;
    }
}
