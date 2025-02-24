package com.springboot.exception;

import lombok.Getter;

public enum ExceptionCode {
    USER_NOT_FOUND(404, "User not found"),
    USER_EXISTS(409, "User exists"),
    QUESTION_NOT_FOUND(404, "Question not found"),
    ANSWER_ALREADY_EXISTS(409, "Answer already exists"),
    QUESTION_FORBIDDEN(403, "Forbidden Question"),
    LIKE_ALREADY_EXISTS(409, "Like already exists"),
    LIKE_NOT_FOUND(404, "Like not found"),
    USER_NOT_CREATED(400, "User not created"),
    USER_FORBIDDEN(403, "Forbidden User"),
    ANSWER_NOT_FOUND(404, "Answer not found"),
    FORBIDDEN_ACCESS(403, "비밀글입니다");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
