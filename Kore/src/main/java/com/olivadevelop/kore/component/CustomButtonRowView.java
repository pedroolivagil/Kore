package com.olivadevelop.kore.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.olivadevelop.kore.databinding.CompButtonRowBinding;

public class CustomButtonRowView extends KoreComponentView<CompButtonRowBinding> {

    public CustomButtonRowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void init(Context context, @Nullable AttributeSet attrs) {
        getBinding().getRoot().setOnClickListener(v -> {
            if (getOnClick() != null) { getOnClick().run(this); }
        });
    }
    @Override
    public void setValue(Object s) { if (s != null) { setValue(String.valueOf(s)); } }
    @Override
    protected void configureFromLayout(@NonNull ComponentAttributes c) {
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
    }
}