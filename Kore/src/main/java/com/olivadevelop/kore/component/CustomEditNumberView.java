package com.olivadevelop.kore.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.olivadevelop.kore.R;
import com.olivadevelop.kore.databinding.CompCustomEditTextBinding;
import com.olivadevelop.kore.databinding.DisabledOverlayBinding;

import org.apache.commons.lang3.StringUtils;

public class CustomEditNumberView extends KoreComponentView<CompCustomEditTextBinding> {

    public CustomEditNumberView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
    @Override
    protected void configureFromLayout(ComponentAttributes c) {
        setMandatory(c.isMandatory());
        getRequiredViewWarning().setVisibility(c.isMandatory() ? View.VISIBLE : View.GONE);
        getBinding().textInputLayout.setErrorEnabled(c.isMandatory());
        if (StringUtils.isNotBlank(c.getHintText())) { setHint(c.getHintText()); }
        if (c.getHintTextColor() != -1) { getBinding().textInputLayout.setHintTextColor(ColorStateList.valueOf(c.getHintTextColor())); }
        if (c.getBoxStrokeColor() != -1) { getBinding().textInputLayout.setBoxStrokeColor(c.getBoxStrokeColor()); }
        if (c.getStartIconDrawable() != -1) {
            getBinding().textInputLayout.setStartIconDrawable(c.getStartIconDrawable());
            if (c.getStartIconTint() != -1) { getBinding().textInputLayout.setStartIconTintList(ColorStateList.valueOf(c.getStartIconTint())); }
        }
    }
    @Override
    public Object getValue() { return getText(); }
    @Override
    public View getRequiredViewWarning() { return getBinding().requiredWarningLabel; }
    @Override
    protected DisabledOverlayBinding getDisabledOverlay() { return getBinding().disabledView; }
    @Override
    public void clearValue() { getBinding().editText.post(() -> setValue(null)); }
    @Override
    protected EditText editTextToValidate() { return getBinding().editText; }
    @Override
    public void setHint(String hint) { getBinding().textInputLayout.setHint(hint); }
    @Override
    public String getHint() { return String.valueOf(getBinding().textInputLayout.getHint()); }
    @Override
    public String getText() {
        Editable text = getBinding().editText.getText();
        return text != null ? text.toString() : null;
    }
    @Override
    public void setValue(Object s) {
        editTextToValidate().setText(null);
        if (s != null && StringUtils.isNotBlank(String.valueOf(s))) { editTextToValidate().setText(String.valueOf(s)); }
    }
    @Override
    protected void whenRegexIsNotValid() {
        getBinding().textInputLayout.setError(getErrorMessageFieldType());
        getBinding().textInputLayout.setErrorIconDrawable(R.drawable.ic_error_min);
    }
    @Override
    protected void whenAfterTextChanged(Editable e) { super.setValue(e.toString()); }
    @Override
    protected void whenRegexIsValid() { getBinding().textInputLayout.setError(null); }
    @Override
    public void setMaxLines(int maxLines) { editTextToValidate().setMaxLines(maxLines); }
    @Override
    public void setMaxLength(int maxLength) { editTextToValidate().setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)}); }
}
