package com.olivadevelop.kore.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.olivadevelop.kore.activity.KoreActivity;

import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OnePickerVisualMediaResult implements IPickerVisualMediaResult {
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImagesLauncher;
    private Consumer<List<Uri>> result;
    private long maxImages = 1;
    public OnePickerVisualMediaResult(KoreActivity<?, ?> activity) {
        pickImagesLauncher = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> getResult().accept(transform(uri)));
    }
    private List<Uri> transform(Uri uri) { return List.of(uri); }
}
