package com.olivadevelop.kore.ui;

import android.view.View;

import com.olivadevelop.kore.R;

public interface Snackbars {

    static SnackbarBuilder error(View view) {
        return SnackbarBuilder.with(view)
                .backgroundColorRes(R.color.error)
                .textColorRes(R.color.white)
                .indefinite();
    }
    static SnackbarBuilder success(View view) {
        return SnackbarBuilder.with(view)
                .backgroundColorRes(R.color.success)
                .textColorRes(R.color.white)
                .longDuration();
    }
    static SnackbarBuilder info(View view) {
        return SnackbarBuilder.with(view)
                .backgroundColorRes(R.color.info)
                .textColorRes(R.color.white)
                .shortDuration();
    }
}
