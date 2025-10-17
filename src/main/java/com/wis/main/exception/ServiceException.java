package com.wis.main.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class ServiceException extends RuntimeException {

    private final String stringKey;
    private final List<Object> args;
    private final HttpStatus httpStatus;
    private final String detail;
    private final boolean hasDetail;

    public ServiceException(HttpStatus httpStatus,
                            String stringKey,
                            boolean hasDetail,
                            String detail,
                            List<Object> args
                            ) {
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.BAD_REQUEST;
        this.stringKey = stringKey;
        this.hasDetail = hasDetail;
        this.detail = detail;
        this.args = args != null ? args : Collections.emptyList();
    }

    public static ServiceException of(String stringKey) {
        return of(HttpStatus.BAD_REQUEST, stringKey);
    }

    public static ServiceException of(HttpStatus httpStatus, String stringKey) {
        return of(httpStatus, stringKey, null);
    }

    public static ServiceException of(HttpStatus httpStatus, String stringKey, List<Object> args) {
        return new ServiceException(httpStatus, stringKey, true, null, args);
    }


    public static ServiceException withDetail(String stringKey, String detail, List<Object> args) {
        return withDetail(HttpStatus.BAD_REQUEST, stringKey, detail, args);
    }

    public static ServiceException withDetail(HttpStatus httpStatus, String stringKey, String detail, List<Object> args) {
        return new ServiceException(httpStatus, stringKey, true, detail, args);
    }
}
