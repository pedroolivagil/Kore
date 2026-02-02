package com.olivadevelop.kore.security;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager<T extends PermissionContract> {
    private final Activity activity;
    private static final int REQUEST_CODE = 654879521;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }
    @SafeVarargs
    public final void requestPermissions(PermissionContract... permissions) {
        List<String> needed = new ArrayList<>();
        for (PermissionContract p : permissions) {
            if (ContextCompat.checkSelfPermission(activity, p.getValue()) != PackageManager.PERMISSION_GRANTED) { needed.add(p.getValue()); }
        }
        if (!needed.isEmpty()) { ActivityCompat.requestPermissions(activity, needed.toArray(new String[0]), REQUEST_CODE); }
    }
    //    @SafeVarargs
    public final boolean areGranted(PermissionContract... permissions) {
        for (PermissionContract p : permissions) {
            if (p == null) { throw new UnsupportedOperationException("The NULL permission is not valid to be granted"); }
            if (ContextCompat.checkSelfPermission(activity, p.getValue()) != PackageManager.PERMISSION_GRANTED) { return false; }
        }
        return true;
    }
}
//public class PermissionManager {
//    private final Activity activity;
//    private static final int REQUEST_CODE = 654879521;
//    public PermissionManager(Activity activity) { this.activity = activity; }
//    public void requestPermissions(Permission... permissions) {
//        List<String> neededPermissions = new ArrayList<>();
//        for (Permission permission : permissions) {
//            if (ContextCompat.checkSelfPermission(activity, permission.getPermission()) != PackageManager.PERMISSION_GRANTED) {
//                neededPermissions.add(permission.getPermission());
//            }
//        }
//        if (!neededPermissions.isEmpty()) { ActivityCompat.requestPermissions(activity, neededPermissions.toArray(new String[0]), REQUEST_CODE); }
//    }
//    public boolean arePermissionsGranted(Permission... permissions) {
//        for (Permission permission : permissions) {
//            if (ContextCompat.checkSelfPermission(activity, permission.getPermission()) != PackageManager.PERMISSION_GRANTED) { return false; }
//        }
//        return true;
//    }
//}