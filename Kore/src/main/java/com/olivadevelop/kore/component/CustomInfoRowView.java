package com.olivadevelop.kore.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;

import com.olivadevelop.kore.databinding.CompInfoRowBinding;

public class CustomInfoRowView extends KoreComponentView<CompInfoRowBinding> {

    public CustomInfoRowView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    @Override
    public void setValue(Object s) { if (s != null) { setValue(String.valueOf(s)); } }
    @Override
    protected void configureFromLayout(ComponentAttributes c) {
        if (c.getTitle() != null) {
            getBinding().txtTitle.setText(c.getTitle());
            getBinding().txtTitle.setTextColor(c.getTextColor());
            getBinding().txtTitle.setTypeface(getBinding().txtTitle.getTypeface(), c.getTextStyle());
            getBinding().txtTitle.setTextSize(Dimension.SP, c.getTextSize());
        }
        if (c.getValue() != null) {
            getBinding().txtValue.setText(c.getValuePropertyProcessed() != null ? c.getValuePropertyProcessed() : c.getValue());
            getBinding().txtValue.setTextColor(c.getValueTextColor());
            getBinding().txtValue.setTypeface(getBinding().txtValue.getTypeface(), c.getValueTextStyle());
            getBinding().txtValue.setTextSize(Dimension.SP, c.getValueTextSize());
        }
    }
}