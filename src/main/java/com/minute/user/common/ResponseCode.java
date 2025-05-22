package com.minute.user.common;

public interface ResponseCode {
    //HttpStatus 200
    String SUCCESS = "SU";
    //HttpStatus 400
    String VALIDATION_FAILED = "VF";
    String DUPLICATE_ID = "DI";
    String DUPLICATE_EMAIL = "DE";
    String DUPLICATE_NICKNAME = "DN";
    String DUPLICATE_PHONE = "DP";
    String NOT_EXISTED_USER = "NU";

    //Http Status 401
    String SIGN_IN_FAIL = "SF";
    String AUTHORIZATION_FAIL = "AF";
    String INVALID_PASSWORD = "IP";

    //Http Status 403
    String NO_PERMISSION = "NP";

    //Http Status 500
    String DATABASE_ERROR = "DE";
}
