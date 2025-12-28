package com.olivadevelop.kore.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.annotation.RegularExpressionField;
import com.olivadevelop.kore.preferences.PreferenceField;
import com.olivadevelop.kore.util.AutoCalculateFormulaData;
import com.olivadevelop.kore.util.Utils;
import com.olivadevelop.kore.R;
import com.olivadevelop.kore.databinding.DisabledOverlayBinding;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
public abstract class KoreComponentView<T extends ViewBinding> extends LinearLayout implements View.OnClickListener, View.OnTouchListener {

    @Setter
    private boolean hasCleanPending;
    @Setter
    private boolean immediateValidation;
    @Setter
    private AutoCalculateFormulaData autocalculateFormula;
    @Setter
    private KoreActivity<?, ?> activity;
    @Setter
    private OnValueChangeAutocalcule onValueChangeAutocalcule;
    @Setter
    private OnValueChange onValueChange;
    @Setter
    private OnClick onClick;
    @Setter(AccessLevel.PROTECTED)
    private PreferenceField preferenceKey;
    private boolean disabled;
    private boolean mandatory;
    private String errorMessageFieldType;
    private String regexPattern = ".*";
    private Map<Class<?>, List<? extends Annotation>> property;
    private final T binding;
    private final DisabledOverlayBinding disabledOverlayBinding;

    public KoreComponentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.activity = context instanceof KoreActivity<?, ?> ? (KoreActivity<?, ?>) context : null;
        this.binding = Utils.Reflex.initBinding(this);
        setDisabled(false);
        this.disabledOverlayBinding = getDisabledOverlayBinding();
        setupValidation(editTextToValidate());
        init(context, attrs);
        setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) { this.activity.scrollTo(0, v.getBottom()); } });
        if (attrs != null) { processAttrs(context, attrs); }
    }
    public final KoreComponentView<T> setProperty(Map<Class<?>, List<? extends Annotation>> property) {
        this.property = property;
        setType();
        return this;
    }
    public final void setRegexPattern(String regex) {
        if (regex == null) { throw new RuntimeException(String.format("Regex for '%s' is invalid.", getClass().getSimpleName())); }
        this.regexPattern = regex;
    }
    public final void addOnFocusEditText(View root, View fv, @ColorRes int colorNormal, @ColorRes int colorFocused) {
        root.setOnFocusChangeListener((v, hasFocus) -> fv.setBackgroundColor(ContextCompat.getColor(root.getContext(), hasFocus ? colorFocused : colorNormal)));
    }
    private void setType() {
        EditText e = editTextToValidate();
        if (e == null) { return; }
        e.setInputType(Utils.getInputTypeFromClass(this.property));
        getProperty().forEach((property, annotations) -> {
            RegularExpressionField regex =
                    annotations.stream().filter(p -> p instanceof RegularExpressionField).map(a -> (RegularExpressionField) a).findFirst().orElse(null);
            post(() -> this.errorMessageFieldType = Utils.intStringFromInputType(getActivity(), property, regex));
        });
    }
    private void setupValidation(EditText e) {
        if (e == null) { return; }
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { whenBeforeTextChanged(s, start, count, after); }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { whenOnTextChanged(s, start, before, count); }
            @Override
            public void afterTextChanged(Editable s) {
                executeListeners(s);
                if (!hasCleanPending) {
                    validateInput();
                    whenAfterTextChanged(s);
                }
                hasCleanPending = false;
            }
        });
    }
    public boolean isValid() {
        if (this.mandatory && StringUtils.isBlank(getText())) { return false; }
        if (this.regexPattern != null && (this.mandatory || StringUtils.isNotBlank(getText()))) { return Pattern.matches(this.regexPattern, getText()); }
        return true;
    }
    private void executeListeners(Editable s) { if (onValueChangeAutocalcule != null) { onValueChangeAutocalcule.run(s); } }
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        if (getDisabledOverlay() != null) { getDisabledOverlay().getRoot().setVisibility(this.disabled ? View.VISIBLE : View.GONE); }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        KoreComponentView<?> that = (KoreComponentView<?>) o;
        return this.mandatory == that.mandatory && Objects.equals(this.binding, that.binding) && Objects.equals(this.regexPattern, that.regexPattern) && Objects.equals(this.property, that.property) && Objects.equals(this.activity, that.activity);
    }
    @Override
    public int hashCode() { return Objects.hash(this.mandatory, this.binding, this.regexPattern, this.property, this.activity); }
    @Override
    public void onClick(View v) { }
    @Override
    public boolean onTouch(View v, MotionEvent event) { return false; }
    protected abstract void init(Context context, @Nullable AttributeSet attrs);
    protected abstract DisabledOverlayBinding getDisabledOverlay();
    protected void configureFromLayout(ComponentAttributes c) { }
    protected void previewEditMode(ComponentAttributes c) { }
    public void setValue(Object s) { }
    public void setHint(String hint) { }
    public void setMaxLines(int maxLines) { }
    public void setMaxLength(int maxLength) { }
    public void setMinLength(int minLength) { }
    public String getText() { return null; }
    public String getHint() { return null; }
    protected EditText editTextToValidate() { return null; }
    protected void whesRegexIsNotValid() { }
    protected void whenRegexIsValid() { }
    protected void whenBeforeTextChanged(CharSequence s, int start, int count, int after) { }
    protected void whenOnTextChanged(CharSequence s, int start, int before, int count) { }
    protected void whenAfterTextChanged(Editable e) { }
    protected void addOnClickListenerToView(View v) { v.setOnClickListener(this); }
    protected void addOnTouchListenerToView(View v) { v.setOnTouchListener(this); }
    private void validateInput() { if (isValid()) { whenRegexIsValid(); } else { whesRegexIsNotValid(); } }
    private void processAttrs(Context context, AttributeSet attrs) {
        int defaultTextStyle = 0;
        float defaultTextSize = 16f;
        int defaultTextColor = context.getResources().getColor(R.color.text_primary, context.getTheme());
//        if (isInEditMode()) {
//            String namespace = "http://schemas.android.com/app";
//            ComponentAttributes.ComponentAttributesBuilder cb = ComponentAttributes.builder()
//                    .checked(ComponentAttributes.getBoolean(attrs, namespace, "checked", false))
//                    .enabled(ComponentAttributes.getBoolean(attrs, namespace, "enabled", true))
//                    .preferenceKey(attrs.getAttributeValue(namespace, "preferenceKey"))
//                    .preferenceType(attrs.getAttributeIntValue(R.styleable.BasicComponentView_preferenceType, -1))
//                    .subtitle(attrs.getAttributeValue(namespace, "subtitle"))
//                    .textColor(ComponentAttributes.getInt(attrs, namespace, "textColor", defaultTextColor))
//                    .textSize(ComponentAttributes.getFloat(attrs, namespace, "textSize", defaultTextSize))
//                    .textStyle(ComponentAttributes.getInt(attrs, namespace, "textStyle", defaultTextStyle))
//                    .title(attrs.getAttributeValue(namespace, "title"))
//                    .value(attrs.getAttributeValue(namespace, "value"))
//                    .valueProperties(attrs.getAttributeValue(namespace, "valueProperties"))
//                    .valueTextColor(ComponentAttributes.getInt(attrs, namespace, "valueTextColor", defaultTextColor))
//                    .valueTextSize(ComponentAttributes.getFloat(attrs, namespace, "valueTextSize", defaultTextSize))
//                    .valueTextStyle(ComponentAttributes.getInt(attrs, namespace, "valueTextStyle", defaultTextStyle));
//            if (cb.valueProperties != null && cb.valueProperties.isEmpty()) {
//                String[] properties = cb.valueProperties.split(";");
//                if (cb.value.contains("%s") && properties.length > 0) {
//                    cb.valuePropertyProcessed(Utils.Reflex.processAllValueProperties(getActivity(), properties));
//                }
//            }
//            previewEditMode(cb.build());
//        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BasicComponentView);
        ComponentAttributes.ComponentAttributesBuilder cb = ComponentAttributes.builder()
                .checked(a.getBoolean(R.styleable.BasicComponentView_checked, false))
                .enabled(a.getBoolean(R.styleable.BasicComponentView_enabled, true))
                .preferenceKey(a.getString(R.styleable.BasicComponentView_preferenceKey))
                .preferenceType(a.getInt(R.styleable.BasicComponentView_preferenceType, -1))
                .subtitle(a.getString(R.styleable.BasicComponentView_subtitle))
                .textColor(a.getColor(R.styleable.BasicComponentView_textColor, defaultTextColor))
                .textSize(a.getDimension(R.styleable.BasicComponentView_textSize, defaultTextSize))
                .textStyle(a.getInt(R.styleable.BasicComponentView_textStyle, defaultTextStyle))
                .title(a.getString(R.styleable.BasicComponentView_title))
                .value(a.getString(R.styleable.BasicComponentView_value))
                .valueProperties(a.getString(R.styleable.BasicComponentView_valueProperties))
                .valueTextColor(a.getColor(R.styleable.BasicComponentView_valueTextColor, defaultTextColor))
                .valueTextSize(a.getDimension(R.styleable.BasicComponentView_valueTextSize, defaultTextSize))
                .valueTextStyle(a.getInt(R.styleable.BasicComponentView_valueTextStyle, defaultTextStyle));
        if (cb.valueProperties != null && !cb.valueProperties.isEmpty()) {
            String[] properties = cb.valueProperties.split(";");
            if (cb.value.contains("%s") && properties.length > 0) {
                cb.valuePropertyProcessed(Utils.Reflex.processAllValueProperties(getActivity(), properties));
            }
        }
        if (cb.preferenceKey != null && cb.preferenceType != -1) {
            setPreferenceKey(new PreferenceField(cb.preferenceKey, PreferenceField.getType(cb.preferenceType)));
        }
        configureFromLayout(cb.build());
        a.recycle();
    }
    public interface OnValueChangeAutocalcule {
        void run(Editable s);
    }

    public interface OnValueChange {
        void run(KoreComponentView<?> s);
    }

    public interface OnClick {
        void run(KoreComponentView<?> s);
    }

    @Getter
    @Builder
    @ToString
    public static class ComponentAttributes {
        private final boolean enabled;
        private final boolean checked;
        private final String preferenceKey;
        private final int preferenceType;
        private final String title;
        private final int textColor;
        private final float textSize;
        private final int textStyle;
        private final String subtitle;
        private final String value;
        private final int valueTextColor;
        private final float valueTextSize;
        private final int valueTextStyle;
        private final String valueProperties;
        @Builder.Default
        private final String valuePropertyProcessed = null;
        private static boolean getBoolean(AttributeSet attrs, String ns, String key, boolean def) {
            String raw = attrs.getAttributeValue(ns, key);
            return raw == null ? def : Boolean.parseBoolean(raw);
        }
        private static int getInt(AttributeSet attrs, String ns, String key, int def) {
            return attrs.getAttributeIntValue(ns, key, def);
        }
        private static float getFloat(AttributeSet attrs, String ns, String key, float def) {
            return attrs.getAttributeFloatValue(ns, key, def);
        }
    }
}