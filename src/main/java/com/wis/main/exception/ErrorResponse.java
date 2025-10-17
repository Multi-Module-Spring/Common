package com.wis.main.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String method;
    private final String path;
    private final String errorCode;
    private final String logBug;

    public static ErrorResponse of(int status, String error, String message, String path, String errorCode) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .method(HttpMethod.GET.name())
                .message(message)
                .path(path)
                .errorCode(errorCode)
                .build();
    }


    public static ErrorResponse of(int status, String error, String message, String path, String errorCode, String logBug) {
        log.error(logBug);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .method(HttpMethod.GET.name())
                .errorCode(errorCode)
                .build();
    }

    public static ErrorResponse of(int status, String error, String message, String path,HttpMethod method, String errorCode, String logBug) {
        log.error(logBug);
        return of(status,error,message,path,method,errorCode,List.of());
    }

    public static ErrorResponse of(int status, String error, String message, String path, HttpMethod method, String errorCode, List<Object> args) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(String.format(message, args))
                .path(path)
                .method(method.name())
                .errorCode(errorCode)
                .build();
    }
}
