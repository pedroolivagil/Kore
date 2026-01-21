package com.olivadevelop.kore.ui;

import android.content.Context;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.olivadevelop.kore.R;

import lombok.Builder;

@Builder
public class SnackbarBuilder {

    private final View anchorView;
    private final Context context;

    private CharSequence message;
    private int duration; // 0 = Snackbar.LENGTH_LONG

    private CharSequence actionText;
    private CharSequence actionSnackbarText;
    private View.OnClickListener actionListener;

    private Integer backgroundColor;
    private Integer actionTextColor;
    private Integer textColor;

    private Snackbar.Callback callback;
    private SnackbarOnClickListener actionSnackbarListener;

    private boolean dismissible;

    public static SnackbarBuilder with(View anchorView) {
        return SnackbarBuilder.builder().anchorView(anchorView).context(anchorView.getContext()).build();
    }
    public SnackbarBuilder message(@StringRes int resId, Object... params) {
        this.message = context.getString(resId, params);
        return this;
    }
    public SnackbarBuilder message(CharSequence message) {
        this.message = message;
        return this;
    }
    public SnackbarBuilder shortDuration() {
        this.duration = Snackbar.LENGTH_SHORT;
        return this;
    }
    public SnackbarBuilder longDuration() {
        this.duration = Snackbar.LENGTH_LONG;
        return this;
    }
    public SnackbarBuilder indefinite() {
        this.duration = Snackbar.LENGTH_INDEFINITE;
        return this;
    }
    public SnackbarBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }
    public SnackbarBuilder action(@StringRes int textRes, View.OnClickListener listener) {
        this.actionText = context.getText(textRes);
        this.actionListener = listener;
        return this;
    }
    public SnackbarBuilder action(CharSequence text, View.OnClickListener listener) {
        this.actionText = text;
        this.actionListener = listener;
        return this;
    }
    public SnackbarBuilder action(@StringRes int textRes, SnackbarOnClickListener listener) {
        this.actionSnackbarText = context.getText(textRes);
        this.actionSnackbarListener = listener;
        return this;
    }
    public SnackbarBuilder action(CharSequence text, SnackbarOnClickListener listener) {
        this.actionSnackbarText = text;
        this.actionSnackbarListener = listener;
        return this;
    }
    public SnackbarBuilder backgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        return this;
    }
    public SnackbarBuilder backgroundColorRes(@ColorRes int colorRes) {
        this.backgroundColor = ContextCompat.getColor(context, colorRes);
        return this;
    }
    public SnackbarBuilder textColor(@ColorInt int color) {
        this.textColor = color;
        return this;
    }
    public SnackbarBuilder textColorRes(@ColorRes int colorRes) {
        this.textColor = ContextCompat.getColor(context, colorRes);
        return this;
    }
    public SnackbarBuilder actionTextColor(@ColorInt int color) {
        this.actionTextColor = color;
        return this;
    }
    public SnackbarBuilder actionTextColorRes(@ColorRes int colorRes) {
        this.actionTextColor = ContextCompat.getColor(context, colorRes);
        return this;
    }
    public SnackbarBuilder callback(Snackbar.Callback callback) {
        this.callback = callback;
        return this;
    }
    public SnackbarBuilder dismissible() {
        this.dismissible = true;
        return this;
    }
    public void show() {
        if (anchorView == null || context == null) { throw new IllegalStateException("Snackbar view is required"); }
        if (message == null) { throw new IllegalStateException("Snackbar message is required"); }
        Snackbar snackbar = Snackbar.make(anchorView, message, duration);
        if (backgroundColor != null) { snackbar.setBackgroundTint(backgroundColor); }
        if (textColor != null) { snackbar.setTextColor(textColor); }
        if (actionText != null && actionListener != null) { snackbar.setAction(actionText, actionListener); }
        if (actionTextColor != null) { snackbar.setActionTextColor(actionTextColor); }
        if (callback != null) { snackbar.addCallback(callback); }
        if (actionSnackbarListener != null) { snackbar.setAction(actionSnackbarText, v -> actionSnackbarListener.onClick(snackbar)); }
        if (dismissible) { snackbar.setAction(R.string.btn_dismiss_snackbar, v -> snackbar.dismiss()); }
        snackbar.show();
    }
}