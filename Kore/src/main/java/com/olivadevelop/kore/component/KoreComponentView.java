package com.olivadevelop.kore.component;

import android.app.Activity;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.textfield.TextInputLayout;
import com.olivadevelop.kore.R;
import com.olivadevelop.kore.activity.KoreActivity;
import com.olivadevelop.kore.annotation.RegularExpressionField;
import com.olivadevelop.kore.annotation.RegularExpressionOption;
import com.olivadevelop.kore.component.attribute.KoreAttributes;
import com.olivadevelop.kore.databinding.DisabledOverlayBinding;
import com.olivadevelop.kore.preferences.PreferenceField;
import com.olivadevelop.kore.preferences.PreferencesHelper;
import com.olivadevelop.kore.util.AutoCalculateFormulaData;
import com.olivadevelop.kore.util.Utils;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
public abstract class KoreComponentView<T extends ViewBinding> extends LinearLayout implements View.OnClickListener, View.OnTouchListener {

    /**
     * Receives the activity to which the component belongs unless the "koreActivity" property is defined.
     *
     * @see KoreComponentView.koreActivity
     */
    @Setter
    private Activity activity;
    @Setter
    private boolean hasCleanPending;
    @Setter
    private boolean immediateValidation;
    @Setter
    private AutoCalculateFormulaData autoCalculateFormula;
    @Setter
    private KoreActivity<?, ?> koreActivity;
    @Setter
    private OnValueChangeAutoCalcule onValueChangeAutocalcule;
    @Setter
    private OnValueChange onValueChange;
    @Setter
    private OnClick onClick;
    @Setter
    private ComponentProperty componentProperty;
    @Setter
    private boolean usePreferences;
    @Setter
    private Function<KoreComponentView<?>, Boolean> validation;
    @Setter(AccessLevel.PROTECTED)
    private PreferenceField preferenceKey;
    private boolean mandatory;
    private boolean disabled;
    private String errorMessageFieldType;
    private String regexPattern = ".*";
    private Map<Class<?>, List<? extends Annotation>> property;
    private final T binding;
    private final DisabledOverlayBinding disabledOverlayBinding;

    private final KoreAttributes attributes = new KoreAttributes();

    public KoreComponentView(Context context, List<RegularExpressionOption> options) {
        this(context, (AttributeSet) null);
        processAttrsFromOptions(options);
    }
    public KoreComponentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.koreActivity = context instanceof KoreActivity<?, ?> ? (KoreActivity<?, ?>) context : null;
        if (this.koreActivity == null) { this.activity = context instanceof Activity ? (Activity) context : null; }
        this.binding = Utils.Reflex.initBinding(this);
        this.disabledOverlayBinding = getDisabledOverlayBinding();
        setDisabled(false);
        setupValidation(editTextToValidate());
        init(context, attrs);
        setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) { this.koreActivity.scrollTo(0, v.getBottom()); } });
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
            RegularExpressionField regex = annotations.stream()
                    .filter(p -> p instanceof RegularExpressionField)
                    .map(a -> (RegularExpressionField) a)
                    .findFirst()
                    .orElse(null);
            post(() -> this.errorMessageFieldType = Utils.intStringFromInputType(getKoreActivity(), property, regex));
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
        if (this.disabled) { return true; }
        if (validation != null) { return validation.apply(this); }
        if (this.mandatory && StringUtils.isBlank(getText())) { return false; }
        if (this.regexPattern != null && (this.mandatory || StringUtils.isNotBlank(getText()))) { return Pattern.matches(this.regexPattern, getText()); }
        return true;
    }
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        if (getDisabledOverlay() != null) { getDisabledOverlay().getRoot().setVisibility(this.disabled ? View.VISIBLE : View.GONE); }
    }
    private void executeListeners(Editable s) { if (onValueChangeAutocalcule != null) { onValueChangeAutocalcule.run(s); } }
    @Override
    public void onClick(View v) { }
    @Override
    public boolean onTouch(View v, MotionEvent event) { return false; }
    protected void init(Context context, @Nullable AttributeSet attrs) { }
    protected void configureFromLayout(@NonNull ComponentAttributes c) { }
    protected void previewEditMode(ComponentAttributes c) { configureFromLayout(c); }
    protected DisabledOverlayBinding getDisabledOverlay() { return null; }
    public View getRequiredViewWarning() { return null; }
    public Object getValue() { return null; }
    public void setValue(Object s) { }
    public void clearValue() { }
    public void setHint(String hint) { }
    public void setMaxLines(int maxLines) { }
    public void setMaxLength(int maxLength) { }
    public void setMinLength(int minLength) { }
    public String getText() { return null; }
    public String getHint() { return null; }
    public void validateInput() { if (isValid()) { whenRegexIsValid(); } else { whenRegexIsNotValid(); } }
    public void setErrorEnabled(boolean enabled) {
        TextInputLayout t = getTextInputLayout();
        if (t != null) { t.setErrorEnabled(enabled); }
    }
    protected TextInputLayout getTextInputLayout() { return null; }
    protected EditText editTextToValidate() { return null; }
    protected void whenRegexIsNotValid() { }
    protected void whenRegexIsValid() { }
    protected void whenBeforeTextChanged(CharSequence s, int start, int count, int after) { }
    protected void whenOnTextChanged(CharSequence s, int start, int before, int count) { }
    protected void whenAfterTextChanged(Editable e) { }
    protected void addOnClickListenerToView(View v) { v.setOnClickListener(this); }
    protected void addOnTouchListenerToView(View v) { v.setOnTouchListener(this); }
    private void processAttrs(Context context, AttributeSet attrs) {
        int defaultTextStyle = 0;
        float defaultTextSize = 16f;
        int defaultTextColor = context.getResources().getColor(R.color.text_primary, context.getTheme());
        int defaultBorderColor = context.getResources().getColor(R.color.gray, context.getTheme());
        int defaultBoxStrokeColor = context.getResources().getColor(R.color.gray, context.getTheme());
        if (isInEditMode()) {
            String namespace = "http://schemas.android.com/app";
            ComponentAttributes.ComponentAttributesBuilder cb = ComponentAttributes.builder()
                    .checked(ComponentAttributes.getBoolean(attrs, namespace, "checked", false))
                    .enabled(ComponentAttributes.getBoolean(attrs, namespace, "enabled", true))
                    .preferenceKey(attrs.getAttributeValue(namespace, "preferenceKey"))
                    .preferenceType(attrs.getAttributeIntValue(R.styleable.KoreComponentView_preferenceType, -1))
                    .subtitle(attrs.getAttributeValue(namespace, "subtitle"))
                    .hintText(attrs.getAttributeValue(namespace, "hintText"))
                    .borderColor(ComponentAttributes.getInt(attrs, namespace, "borderColor", defaultBorderColor))
                    .textColor(ComponentAttributes.getInt(attrs, namespace, "textColor", defaultTextColor))
                    .textSize(ComponentAttributes.getFloat(attrs, namespace, "textSize", defaultTextSize))
                    .textStyle(ComponentAttributes.getInt(attrs, namespace, "textStyle", defaultTextStyle))
                    .title(attrs.getAttributeValue(namespace, "title"))
                    .value(attrs.getAttributeValue(namespace, "value"))
                    .valueProperties(attrs.getAttributeValue(namespace, "valueProperties"))
                    .valueTextColor(ComponentAttributes.getInt(attrs, namespace, "valueTextColor", defaultTextColor))
                    .valueTextSize(ComponentAttributes.getFloat(attrs, namespace, "valueTextSize", defaultTextSize))
                    .valueTextStyle(ComponentAttributes.getInt(attrs, namespace, "valueTextStyle", defaultTextStyle))
                    .mandatory(ComponentAttributes.getBoolean(attrs, namespace, "mandatory", false))
                    .errorEnabled(ComponentAttributes.getBoolean(attrs, namespace, "errorEnabled", false))
                    .hintTextColor(ComponentAttributes.getInt(attrs, namespace, "hintTextColor", defaultTextColor))
                    .boxStrokeColor(ComponentAttributes.getInt(attrs, namespace, "borderColor", defaultBoxStrokeColor))
                    .startIconDrawable(attrs.getAttributeResourceValue(namespace, "startIconDrawable", -1))
                    .startIconTint(ComponentAttributes.getInt(attrs, namespace, "startIconTint", -1))
                    .spanCount(ComponentAttributes.getInt(attrs, namespace, "spanCount", 3))
                    .maxImages(ComponentAttributes.getInt(attrs, namespace, "maxImages", 1));
            if (cb.valueProperties != null && cb.valueProperties.isEmpty()) {
                String[] properties = cb.valueProperties.split(";");
                if (cb.value.contains("%s") && properties.length > 0) {
                    cb.valuePropertyProcessed(Utils.Reflex.processAllValueProperties(getKoreActivity(), properties));
                }
            }
            previewEditMode(cb.build());
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KoreComponentView);
        ComponentAttributes.ComponentAttributesBuilder cb =
                ComponentAttributes.builder().checked(a.getBoolean(R.styleable.KoreComponentView_checked, false))
                        .enabled(a.getBoolean(R.styleable.KoreComponentView_enabled, true))
                        .preferenceKey(a.getString(R.styleable.KoreComponentView_preferenceKey))
                        .preferenceType(a.getInt(R.styleable.KoreComponentView_preferenceType, -1))
                        .subtitle(a.getString(R.styleable.KoreComponentView_subtitle))
                        .borderColor(a.getColor(R.styleable.KoreComponentView_borderColor, defaultBorderColor))
                        .textColor(a.getColor(R.styleable.KoreComponentView_textColor, defaultTextColor))
                        .textSize(a.getDimension(R.styleable.KoreComponentView_textSize, defaultTextSize))
                        .textStyle(a.getInt(R.styleable.KoreComponentView_textStyle, defaultTextStyle))
                        .title(a.getString(R.styleable.KoreComponentView_title))
                        .value(a.getString(R.styleable.KoreComponentView_value))
                        .valueProperties(a.getString(R.styleable.KoreComponentView_valueProperties))
                        .valueTextColor(a.getColor(R.styleable.KoreComponentView_valueTextColor, defaultTextColor))
                        .valueTextSize(a.getDimension(R.styleable.KoreComponentView_valueTextSize, defaultTextSize))
                        .valueTextStyle(a.getInt(R.styleable.KoreComponentView_valueTextStyle, defaultTextStyle))
                        .mandatory(a.getBoolean(R.styleable.KoreComponentView_mandatory, false))
                        .errorEnabled(a.getBoolean(R.styleable.KoreComponentView_errorEnabled, false))
                        .hintTextColor(a.getColor(R.styleable.KoreComponentView_hintTextColor, defaultTextColor))
                        .boxStrokeColor(a.getColor(R.styleable.KoreComponentView_borderColor, defaultBoxStrokeColor))
                        .startIconDrawable(a.getResourceId(R.styleable.KoreComponentView_startIconDrawable, -1))
                        .startIconTint(a.getColor(R.styleable.KoreComponentView_startIconTint, -1))
                        .hintText(a.getString(R.styleable.KoreComponentView_hintText))
                        .spanCount(a.getInt(R.styleable.KoreComponentView_spanCount, 3))
                        .maxImages(a.getInt(R.styleable.KoreComponentView_maxImages, 1));
        if (cb.valueProperties != null && !cb.valueProperties.isEmpty()) {
            String[] properties = cb.valueProperties.split(";");
            if (cb.value.contains("%s") && properties.length > 0) {
                cb.valuePropertyProcessed(Utils.Reflex.processAllValueProperties(getKoreActivity(), properties));
            }
        }
        if (cb.preferenceKey != null && cb.preferenceType != -1) {
            setPreferenceKey(new PreferenceField(cb.preferenceKey, PreferenceField.getType(cb.preferenceType)));
        }
        configureFromLayout(cb.build());
        a.recycle();
    }
    private void processAttrsFromOptions(List<RegularExpressionOption> options) {
        ComponentAttributes cb = ComponentAttributes.builder().build();
        options.forEach(opt -> Utils.Reflex.fillProperty(cb, opt.attribute().getXmlName(), opt.value()));
        configureFromLayout(cb);
    }
    protected void updatePreferences(Object value) { PreferencesHelper.getInstance().add(getPreferenceKey(), value); }
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        if (getRequiredViewWarning() == null) { return; }
        getRequiredViewWarning().setVisibility(this.mandatory ? View.VISIBLE : View.GONE);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        KoreComponentView<?> that = (KoreComponentView<?>) o;
        return this.mandatory == that.mandatory && Objects.equals(this.binding, that.binding) && Objects.equals(this.regexPattern, that.regexPattern) && Objects.equals(this.property, that.property) && Objects.equals(this.koreActivity, that.koreActivity);
    }
    @Override
    public int hashCode() { return Objects.hash(this.mandatory, this.binding, this.regexPattern, this.property, this.koreActivity); }
    public interface OnValueChangeAutoCalcule {
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
        //        private final Map<KoreComponentViewAttribute, Object> attributes = new HashMap<>();
        private final boolean enabled;
        private final boolean errorEnabled;
        private final boolean checked;
        private final String preferenceKey;
        private final int preferenceType;
        private final String title;
        private final String hintText;
        private final int borderColor;
        private final int boxStrokeColor;
        private final int textColor;
        private final int hintTextColor;
        private final float textSize;
        private final int textStyle;
        private final String subtitle;
        private final String value;
        private final int valueTextColor;
        private final float valueTextSize;
        private final int valueTextStyle;
        private final String valueProperties;
        private final int startIconDrawable;
        private final int startIconTint;
        private final int spanCount;
        private final int maxImages;
        @Builder.Default
        private final boolean mandatory = false;
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
//        public Object getFromMap(KoreComponentViewAttribute attribute) { return attributes.get(attribute); }
//        public void setToMap(KoreComponentViewAttribute attribute, Object value) { attributes.put(attribute, value); }
    }
}