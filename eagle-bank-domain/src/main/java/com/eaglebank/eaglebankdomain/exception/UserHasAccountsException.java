package com.eaglebank.eaglebankdomain.exception;

public class UserHasAccountsException extends RuntimeException {
    public UserHasAccountsException(String message) {
        super(message);
    }
}
