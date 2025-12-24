package com.olivadevelop.kore.media;

import androidx.annotation.NonNull;

import com.olivadevelop.kore.security.PermissionContract;

public interface CameraPermissionProvider {
    @NonNull
    PermissionContract getCameraPermission();
    @NonNull
    PermissionContract getReadStoragePermission();
}
