package com.olivadevelop.kore.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;

import com.olivadevelop.kore.preferences.PreferencesHelper;
import com.olivadevelop.kore.databinding.CompCustomSwitchBinding;
import com.olivadevelop.kore.databinding.DisabledOverlayBinding;

public class CustomSwitchView extends KoreComponentView<CompCustomSwitchBinding> {

    public CustomSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getBinding().getRoot().setOnClickListener(v -> getBinding().toggleButton.performClick());
        getBinding().toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferencesHelper.getInstance().add(getPreferenceKey(), isChecked);
            if (getOnValueChange() != null) { getOnValueChange().run(this); }
        });
    }

    @Override
    protected void init(Context context, @Nullable AttributeSet attrs) { }
    @Override
    public void setHint(String hint) { getBinding().toggleButton.setText(hint); }
    @Override
    public String getHint() { return String.valueOf(getBinding().toggleButton.getHint()); }
    @Override
    protected DisabledOverlayBinding getDisabledOverlay() { return null; }
    @Override
    public void setValue(Object s) { if (s instanceof Boolean) { getBinding().toggleButton.setChecked((boolean) s); } }
    public final boolean isActive() { return getBinding().toggleButton.isChecked(); }
    @Override
    protected void previewEditMode(ComponentAttributes c) {
        if (c.getTitle() != null) { getBinding().txtTitle.setText(c.getTitle()); }
        if (c.getSubtitle() != null) { getBinding().txtSubtitle.setText(c.getSubtitle()); }
        getBinding().toggleButton.setChecked(c.isChecked());
    }
    @Override
    protected void configureFromLayout(ComponentAttributes c) {
        if (c.getTitle() != null) {
            getBinding().txtTitle.setText(c.getTitle());
            getBinding().txtTitle.setTextColor(c.getTextColor());
            getBinding().txtTitle.setTypeface(getBinding().txtTitle.getTypeface(), c.getTextStyle());
            getBinding().txtTitle.setTextSize(Dimension.SP, c.getTextSize());
        }
        if (c.getSubtitle() != null) {
            getBinding().txtSubtitle.setText(c.getSubtitle());
            getBinding().txtSubtitle.setTextColor(c.getValueTextColor());
            getBinding().txtSubtitle.setTypeface(getBinding().txtSubtitle.getTypeface(), c.getValueTextStyle());
            getBinding().txtSubtitle.setTextSize(Dimension.SP, c.getValueTextSize());
        }
        if (getPreferenceKey() != null) {
            PreferencesHelper.getInstance().get(getPreferenceKey()).ifPresentOrElse(value -> getBinding().toggleButton.setChecked((Boolean) value), () -> {
                PreferencesHelper.getInstance().add(getPreferenceKey(), c.isChecked());
                getBinding().toggleButton.setChecked(c.isChecked());
            });
        }
        setDisabled(!c.isEnabled());
    }
}