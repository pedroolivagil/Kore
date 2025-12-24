package com.olivadevelop.kore.security;

public interface PermissionContract {
    String getValue();
}
//IMPL EXAMPLE
//@Getter
//public enum AppPermission implements PermissionContract {
//
//    INTERNET(Manifest.permission.INTERNET),
//    CAMERA(Manifest.permission.CAMERA),
//    POST_NOTIFICATIONS(Manifest.permission.POST_NOTIFICATIONS),
//    READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
//    WRITE_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//    private final String value;
//
//    AppPermission(String value) {
//        this.value = value;
//    }
//}