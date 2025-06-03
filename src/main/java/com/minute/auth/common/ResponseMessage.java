package com.minute.auth.common;

public interface ResponseMessage {
    //HttpStatus 200
    String SUCCESS = "Success";
    //HttpStatus 400
    String VALIDATION_FAILED = "Validation failed";
    String DUPLICATE_EMAIL = "Duplicate email";
    String DUPLICATE_ID = "Duplicate id";
    String DUPLICATE_NICKNAME = "Duplicate nickname";
    String DUPLICATE_PHONE = "Duplicate phone number";
    String NOT_EXISTED_USER = "This user does not exist";

    //Http Status 401
    String SIGN_IN_FAIL = "Login information mismatch";
    String AUTHORIZATION_FAIL = "Authorization failed";
    String INVALID_PASSWORD = "Wrong Password";
    //Http Status 403
    String NO_PERMISSION = "Does not have permission";

    //Http Status 500
    String DATABASE_ERROR = "Database error";
    String MAIL_FAIL = "Mail send failed";

    // ResponseMessage.java
    String USER_UPDATE_SUCCESS = "User information update success";

}
