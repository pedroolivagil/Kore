package com.olivadevelop.kore_android_tester;

import com.olivadevelop.kore.security.PermissionContract;

public enum TesterPermissions implements PermissionContract {
    INTERNET(android.Manifest.permission.INTERNET),
    CAMERA(android.Manifest.permission.CAMERA),
    POST_NOTIFICATIONS(android.Manifest.permission.POST_NOTIFICATIONS),
    READ_STORAGE(android.Manifest.permission.READ_EXTERNAL_STORAGE),
    WRITE_STORAGE(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    private final String value;
    TesterPermissions(String value) { this.value = value; }
    @Override
    public String getValue() { return value; }
}