package com.ruoyi.common.exception.user;

public class GoogleAuthException extends UserException {
    private static final long serialVersionUID = 1L;

    public GoogleAuthException() {
        super("user.googleauth.error", null);
    }
}
