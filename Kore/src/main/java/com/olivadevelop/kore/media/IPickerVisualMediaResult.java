package com.olivadevelop.kore.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;

import java.util.List;
import java.util.function.Consumer;

public interface IPickerVisualMediaResult {
    ActivityResultLauncher<PickVisualMediaRequest> getPickImagesLauncher();
    Consumer<List<Uri>> getResult();
    void setResult(Consumer<List<Uri>> result);
    long getMaxImages();
    void setMaxImages(long maxImages);
}
