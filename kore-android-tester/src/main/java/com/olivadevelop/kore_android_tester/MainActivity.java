package com.olivadevelop.kore_android_tester;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.attributtes.KoreComponentViewAttribute;
import com.olivadevelop.kore.security.PermissionContract;
import com.olivadevelop.kore_android_tester.databinding.ActivityMainBinding;

public class MainActivity extends KoreActivity<ActivityMainBinding, MainActivityViewModel> {
    @NonNull
    @Override
    public PermissionContract getCameraPermission() { return TesterPermissions.CAMERA; }
    @NonNull
    @Override
    public PermissionContract getReadStoragePermission() {
        return TesterPermissions.READ_STORAGE;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        KoreComponentViewAttribute.MANDATORY.getXmlName();
    }
}