package com.example.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * Created by Android Studio.
 * User: CaLLIek
 * Date: 11.02.2020
 * Time: 12:26
 */

public class Permissions extends Activity {
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.MODIFY_PHONE_STATE
    };

    static final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    static public void requestPermissions(final Activity activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
    }

    static public boolean hasPermissions(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && PERMISSIONS != null) {
            for (String permission : PERMISSIONS) {
                //если разрешение не получено приложением вернуть false
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
