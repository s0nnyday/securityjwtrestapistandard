package com.example.securityjwtrestapistandard.exception;

import lombok.Getter;

public class ApiException extends RuntimeException {
    @Getter
    private ExceptionCode exceptionCode;

    public ApiException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
