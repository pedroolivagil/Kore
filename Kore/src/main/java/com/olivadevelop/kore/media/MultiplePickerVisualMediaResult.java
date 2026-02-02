package com.olivadevelop.kore.media;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.olivadevelop.kore.activity.KoreActivity;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MultiplePickerVisualMediaResult implements IPickerVisualMediaResult {
    ActivityResultLauncher<PickVisualMediaRequest> pickImagesLauncher;
    private Consumer<List<Uri>> result;
    private long maxImages;
    public MultiplePickerVisualMediaResult(KoreActivity<?, ?> activity) {
        pickImagesLauncher = activity.registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> getResult().accept(uris.stream().filter(Objects::nonNull).limit(getMaxImages()).collect(Collectors.toList()))
        );
    }
}
