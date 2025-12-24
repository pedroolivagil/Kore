package com.olivadevelop.kore.error;

import androidx.annotation.StringRes;

import lombok.Data;

@Data
public class InvalidPropertyErrorVM {
    private String code;
    private @StringRes int message;
    private Object[] messageArgs;
    public InvalidPropertyErrorVM(String code, @StringRes int message) {
        this.code = code;
        this.message = message;
    }
    public InvalidPropertyErrorVM(String code, @StringRes int message, Object... messageArgs) {
        this.code = code;
        this.message = message;
        this.messageArgs = messageArgs;
    }
}
