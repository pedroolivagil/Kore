package com.olivadevelop.kore.media;

import android.content.Intent;

public interface FilePickerUtils {
    static Intent createExportIntent(String defaultName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, defaultName + ".tsbackup");
        return intent;
    }
    static Intent createImportIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        return intent;
    }
}