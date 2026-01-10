package com.olivadevelop.kore.component;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.R;
import com.olivadevelop.kore.databinding.CompCustomImageSelectorBinding;
import com.olivadevelop.kore.databinding.DisabledOverlayBinding;
import com.olivadevelop.kore.media.CameraGalleryImageManager;

import java.io.FileNotFoundException;

import lombok.Getter;

@Getter
public class CustomImageSelector extends KoreComponentView<CompCustomImageSelectorBinding> {
    private Uri photoUri;

    public CustomImageSelector(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    @Override
    protected void init(Context context, @Nullable AttributeSet attrs) {
        addOnClickListenerToView(getBinding().btnPreviewGallery);
        addOnClickListenerToView(getBinding().btnPreviewCamera);
        getBinding().getRoot().post(() -> getKoreActivity().setListener((requestCode, resultCode, data) -> {
            if (CameraGalleryImageManager.REQUEST_CODE_GALLERY == requestCode) {
                processIntentMediaGallery(data);
            } else if (CameraGalleryImageManager.REQUEST_IMAGE_CAPTURE == requestCode && RESULT_OK == resultCode) {
                processIntentMediaCamera();
            }
        }));
    }
    @Override
    public Object getValue() { return photoUri != null ? photoUri.getPath() : null; }
    @Override
    public View getRequiredViewWarning() { return getBinding().requiredWarningLabel; }
    @Override
    protected DisabledOverlayBinding getDisabledOverlay() { return getBinding().disabledView; }
    @Override
    public String getHint() { return getContext().getString(R.string.default_image_hint); }
    @Override
    public void clearValue() {
        photoUri = null;
        getBinding().imagePreview.setImageDrawable(null);
    }

    @Override
    public void onClick(View v) {
        if (v == getBinding().btnPreviewCamera) {
            this.photoUri = CameraGalleryImageManager.openCamera(getKoreActivity(), v, Constants.Files.DIR_TMP_PREVIEW);
        } else if (v == getBinding().btnPreviewGallery) {
            CameraGalleryImageManager.openGallery(getKoreActivity());
        }
    }
    private void processIntentMediaGallery(Intent data) {
        if (data == null || data.getData() == null) { return; }
        try {
            processMediaImage(data.getData());
            this.photoUri = data.getData();
        } catch (FileNotFoundException e) {
            Log.e(Constants.Log.TAG, "Error on select image from preview color. TRACE: " + e.getMessage());
        }
    }
    private void processIntentMediaCamera() {
        try {
            processMediaImage(this.photoUri);
        } catch (FileNotFoundException e) {
            Log.e(Constants.Log.TAG, "Error on select image from preview color. TRACE: " + e.getMessage());
        }
    }
    private void processMediaImage(Uri imageUri) throws FileNotFoundException {
        CameraGalleryImageManager.loadImage(imageUri, getBinding().imagePreview);
    }
}
