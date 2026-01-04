package com.olivadevelop.kore_android_tester;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.security.PermissionContract;
import com.olivadevelop.kore_android_tester.databinding.ActivityMainBinding;

public class MainActivity extends KoreActivity<ActivityMainBinding, MainActivityViewModel> {
    @NonNull
    @Override
    public PermissionContract getCameraPermission() {
        return null;
    }
    @NonNull
    @Override
    public PermissionContract getReadStoragePermission() {
        return null;
    }
}