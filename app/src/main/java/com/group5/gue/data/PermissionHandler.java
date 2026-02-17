package com.group5.gue.data;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHandler {

    Activity activity;

    public PermissionHandler(Activity activity) {
        this.activity = activity;
    }
    public boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(activity,
                permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String permission) {
        ActivityCompat.requestPermissions(this.activity,
                new String[]{permission},
                1);
    }
}
