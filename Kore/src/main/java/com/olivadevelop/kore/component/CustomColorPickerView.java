package com.olivadevelop.kore.component;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.olivadevelop.kore.Constants;
import com.olivadevelop.kore.R;
import com.olivadevelop.kore.databinding.CompCustomColorSelectorBinding;
import com.olivadevelop.kore.databinding.DisabledOverlayBinding;
import com.olivadevelop.kore.media.CameraGalleryImageManager;
import com.skydoves.colorpickerview.ColorHsvPalette;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import java.io.FileNotFoundException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomColorPickerView extends KoreComponentView<CompCustomColorSelectorBinding> {
    private Uri photoUri;
    private Integer colorSelected;
    private static final Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);

    public CustomColorPickerView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void init(Context context, @Nullable AttributeSet attrs) {
        addOnClickListenerToView(getBinding().btnPreviewGallery);
        addOnClickListenerToView(getBinding().btnPreviewCamera);
        addOnClickListenerToView(getBinding().btnClearImage);
        addOnClickListenerToView(getBinding().btnResetColor);
        addOnClickListenerToView(getBinding().btnOpenComponent);
        addOnClickListenerToView(getBinding().btnCollapse);
        addOnClickListenerToView(getBinding().colorPreviewMinimized);
        addOnTouchListenerToView(getBinding().colorPickerPreview);
        addOnTouchListenerToView(getBinding().brightnessSlide);
        getBinding().colorPickerPreview.post(() -> getActivity().setListener((requestCode, resultCode, data) -> {
            if (CameraGalleryImageManager.REQUEST_CODE_GALLERY == requestCode) {
                processIntentMediaGallery(context, data);
            } else if (CameraGalleryImageManager.REQUEST_IMAGE_CAPTURE == requestCode && RESULT_OK == resultCode) {
                processIntentMediaCamera(context);
            }
        }));
        getBinding().colorPickerPreview.setPaletteDrawable(getColorHsvPalette());
        BrightnessSlideBar brightnessSlideBar = getBinding().brightnessSlide;
        getBinding().colorPickerPreview.attachBrightnessSlider(brightnessSlideBar);
        getBinding().btnClearImage.setVisibility(View.GONE);
        getBinding().btnResetColor.setVisibility(View.VISIBLE);
        getBinding().wrapperColorMax.setVisibility(View.GONE);
        getBinding().wrapperColorMin.setVisibility(View.VISIBLE);
    }
    @NonNull
    private ColorHsvPalette getColorHsvPalette() { return new ColorHsvPalette(getResources(), bitmap); }
    @Override
    public String getHint() { return getContext().getString(R.string.default_color_hint); }
    @Override
    public Object getValue() { return this.colorSelected; }
    @Override
    protected View getRequiredViewWarning() { return getBinding().requiredWarningLabel; }
    @Override
    protected DisabledOverlayBinding getDisabledOverlay() { return getBinding().disabledView; }
    @Override
    public boolean isValid() {
        return !isMandatory() || this.colorSelected != null;
    }
    @Override
    public void setValue(Object s) {
        if (s instanceof Integer) {
            this.colorSelected = (Integer) s;
            setColorToView();
        }
    }
    @Override
    public void clearValue() {
        this.colorSelected = null;
        setColorToView();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == getBinding().colorPickerPreview || v == getBinding().brightnessSlide) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            BrightnessSlideBar brightnessSlider = getBinding().colorPickerPreview.getBrightnessSlider();
            getBinding().btnClearImage.setVisibility(View.GONE);
            getBinding().btnResetColor.setVisibility(View.GONE);
            if (getBinding().colorPickerPreview.isHuePalette() && brightnessSlider != null) {
                this.colorSelected = brightnessSlider.assembleColor();
                getBinding().btnResetColor.setVisibility(View.VISIBLE);
            } else {
                this.colorSelected = getBinding().colorPickerPreview.getPureColor();
                getBinding().btnClearImage.setVisibility(View.VISIBLE);
            }
            setColorToView();
        } else {
            performClick();
        }
        return false;
    }
    @Override
    public void onClick(View v) {
        if (v == getBinding().btnPreviewGallery) {
            CameraGalleryImageManager.openGallery(getActivity());
        } else if (v == getBinding().btnPreviewCamera) {
            this.photoUri = CameraGalleryImageManager.openCamera(getActivity(), v, Constants.Files.DIR_TMP_PREVIEW);
        } else if (v == getBinding().btnClearImage) {
            resetImage();
        } else if (v == getBinding().btnResetColor) {
            resetColor();
        } else if (v == getBinding().colorPreviewMinimized) {
            openSelectorDialog();
        } else if (v == getBinding().btnOpenComponent || v == getBinding().btnCollapse) {
            toggleComponent();
        }
    }
    private void resetColor() {
        getBinding().colorPickerPreview.selectCenter();
        BrightnessSlideBar brightnessSlider = getBinding().colorPickerPreview.getBrightnessSlider();
        if (brightnessSlider == null) { return; }
        brightnessSlider.setSelectorPosition(1f);
        this.colorSelected = brightnessSlider.assembleColor();
        setColorToView();
    }
    private void resetImage() {
        getBinding().btnClearImage.setVisibility(View.GONE);
        getBinding().btnResetColor.setVisibility(View.VISIBLE);
        getBinding().brightnessSlide.setVisibility(View.VISIBLE);
        getBinding().colorPickerPreview.setPaletteDrawable(getColorHsvPalette());
        resetColor();
    }
    private void processIntentMediaGallery(Context context, Intent data) {
        if (data == null || data.getData() == null) { return; }
        try {
            processMediaImage(context, data.getData());
        } catch (FileNotFoundException e) {
            Log.e(Constants.Log.TAG, "Error on select image from preview color. TRACE: " + e.getMessage());
        }
    }
    private void processIntentMediaCamera(Context context) {
        try {
            processMediaImage(context, this.photoUri);
        } catch (FileNotFoundException e) {
            Log.e(Constants.Log.TAG, "Error on select image from preview color. TRACE: " + e.getMessage());
        }
    }
    private void processMediaImage(Context context, Uri imageUri) throws FileNotFoundException {
        CameraGalleryImageManager.ImageTransformConfig.ImageTransformConfigBuilder config = CameraGalleryImageManager.ImageTransformConfig.builder();
        Drawable drawable = CameraGalleryImageManager.getFileFromUri(context, imageUri, config.build());
        getBinding().colorPickerPreview.setPaletteDrawable(drawable);
        getBinding().brightnessSlide.setVisibility(View.GONE);
        getBinding().btnResetColor.setVisibility(View.GONE);
        getBinding().btnClearImage.setVisibility(View.VISIBLE);
    }
    private void toggleComponent() {
        ViewGroup parent;
        if (getBinding().wrapperColorMin.getVisibility() == View.VISIBLE) {
            parent = (ViewGroup) getBinding().wrapperColorMin.getParent();
        } else {
            parent = (ViewGroup) getBinding().wrapperColorMax.getParent();
        }
        TransitionManager.beginDelayedTransition(parent, new AutoTransition());
        if (getBinding().wrapperColorMin.getVisibility() == View.VISIBLE) {
            getBinding().wrapperColorMin.setVisibility(View.GONE);
            getBinding().wrapperColorMax.setVisibility(View.VISIBLE);
        } else {
            getBinding().wrapperColorMax.setVisibility(View.INVISIBLE);
            getBinding().wrapperColorMax.postDelayed(() -> {
                getBinding().wrapperColorMax.setVisibility(View.GONE);
                getBinding().wrapperColorMin.setVisibility(View.VISIBLE);
            }, 350);
        }
    }
    private void openSelectorDialog() {
        new ColorPickerDialog.Builder(getContext()).setTitle("ColorPicker Dialog")
                .setPositiveButton(getContext().getString(R.string.btn_confirm), (ColorEnvelopeListener) (envelope, fromUser) -> {
                    this.colorSelected = envelope.getColor();
                    setColorToView();
                })
                .setNegativeButton(getContext().getString(R.string.dialog_default_cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show();
    }
    private void setColorToView() {
        if (this.colorSelected == null) { this.colorSelected = Color.WHITE; }
        getBinding().colorPreview.setBackgroundTintList(ColorStateList.valueOf(this.colorSelected));
        getBinding().colorPreviewMinimized.setBackgroundTintList(getBinding().colorPreview.getBackgroundTintList());
    }
}
