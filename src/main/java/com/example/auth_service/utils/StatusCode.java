package com.example.auth_service.utils;


public class StatusCode {


    public static final String SUCCESS = "00";


    public static final String EMAIL_ALREADY_EXISTS = "01";
    public static final String PHONE_ALREADY_EXISTS = "02";
    public static final String INVALID_PASSWORD = "03";
    public static final String DUPLICATE_OLD_PASSWORD = "04";
    public static final String USER_NOT_FOUND = "05";
    public static final String TOKEN_INVALID = "06";
    public static final String ROLE_NOT_FOUND = "07";
    public static final String EMAIL_EMPTY = "10";
    public static final String EMAIL_NOT_REGISTERED = "11";

    public static final String INTERNAL_ERROR = "99";
    public static final String VALIDATION_ERROR = "98";

}
