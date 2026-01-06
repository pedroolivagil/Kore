package com.olivadevelop.kore;

import java.io.File;

public abstract class Constants {

    public static class Formats {
        public static final String DD_MM_YYYY_HH_MM_SS = "dd-MM-yyyy HH:mm:ss";
        public static final String DATE_FORMAT_FIELD = "%02d/%02d/%04d";
        public static final String FORMAT_ML = "%s ml";
        public static final String EEE_MMM_D_YYYY = "EEE, MMM d, yyyy";
        public static final String HH_MM_A = "hh:mm a";
        public static final String EEEE_D_MMM_YYYY = "EEEE, d MMMM, yyyy";

        public static final String HH_MM = "HH:mm";
        public static final String D_MMM_YYYY = "d MMMM, yyyy";
        public static final String DD_MM_YYYY = "dd/MM/yyyy";
        public static final String YYYY_MM_DD_HH_MM_SS = "yyyy_MM_dd_HH_mm_ss";
    }

    public static class Defaults {
        public static final int DATABASE_VERSION = 1;
        public static final String PACKAGE_DTO = "com.olivadevelop.kore.db.dto";
    }

    public static class Regex {
        public static final String INTEGER = "^-?(0|[1-9][0-9]{0,8})$";
        public static final String DOUBLE = "^-?[0-9]{1,7}([\\.][0-9]{1,2})?$";
        public static final String STRING_SIMPLE = "^[a-zA-Z0-9\\s-]{1,50}$";
        public static final String STRING_FULL_CHARS = "(?s)^.{1,500}$";
        public static final String REGEX_SLASH = "/";
        public static final String REGEX_CLEAN_SIMPLE = "[^a-z0-9]";
    }

    public static class Provider {
        public static final String CAMERA_PROVIDER = "com.olivadevelop.coreutils.provider";
    }

    public static class IntentParam {
        public static final String INTENT_IMAGE_TYPE = "image/*";
    }

    public static class Field {
        public static final String SCREEN_BACK = "screen_back";
        public static final String FRAGMENT_LOADING = "fragment_loading";
        public static final String FRAGMENT_ARGUMENTS = "fragment_arguments";
    }

    public static class UI {
        public static final String LABEL_FORM = "label_form_";
    }

    public static class Animations {
        public static final String INTRO = "INTRO";
        public static final String LOADER = "LOADER";
    }

    public static class Log {
        public static final String TAG = "//////////////////// COREUTILS LOG";
    }

    public static class Security {
        public static final String ALGORITHM = "PBKDF2WithHmacSHA256";
        public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
        public static final String ALGORITHM_TYPE = "AES";
        public static final String ANDROID_KEYSTORE = "AndroidKeyStore";
        public static final int GCM_IV_LENGTH = 12;
        public static final int SALT_LENGTH = 16;
        public static final int GCM_TAG_LENGTH = 128; // bits
    }

    public static class Files {
        public static final String EXTENSION_JPG = ".jpg";
        public static final String DIR_TMP = "tmp";
        public static final String DIR_TMP_PREVIEW = DIR_TMP + "/preview";
        public static final String SPLITTER = File.separator;
    }
}