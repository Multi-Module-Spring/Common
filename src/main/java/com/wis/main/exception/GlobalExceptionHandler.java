package com.wis.main.exception;

import com.wis.main.util.core_util.CoreBean;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class GlobalExceptionHandler extends CoreBean {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(ServiceException ex, HttpServletRequest request) {

        String resolvedMessage = messageUtil.getI18n(ex.getStringKey(),ex.getDetail(), ex.getArgs());
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getHttpStatus().value(),
                ex.getHttpStatus().getReasonPhrase(),
                resolvedMessage,
                request.getRequestURI(),
                HttpMethod.valueOf(request.getMethod()),
                ex.getStringKey(),
                ex.getDetail()
        );
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex, HttpServletRequest request) {

        String defaultKey = HttpStatus.BAD_REQUEST.name();
        String message = messageUtil.getI18n(defaultKey,ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI(),
                HttpMethod.valueOf(request.getMethod()),
                defaultKey,
                ex.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
