package com.wis.main.exception;

import com.wis.main.configuration.Payload;
import com.wis.main.exception.service.GlobalExceptionService;
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
    private final GlobalExceptionService globalExceptionService;

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(ServiceException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getHttpStatus().value(),
                ex.getHttpStatus().getReasonPhrase(),
                ex.getStringKey(),
                request.getRequestURI(),
                HttpMethod.valueOf(request.getMethod()),
                ex.getStringKey(),
                ex.getDetail(),
                ex.getArgs()
        );

        errorResponse = globalExceptionService.execute(new Payload(),errorResponse);
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex, HttpServletRequest request) {

        String defaultKey = HttpStatus.BAD_REQUEST.name();
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                defaultKey,
                request.getRequestURI(),
                HttpMethod.valueOf(request.getMethod()),
                defaultKey,
                ex.getMessage()
        );
        errorResponse = globalExceptionService.execute(new Payload(),errorResponse);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
