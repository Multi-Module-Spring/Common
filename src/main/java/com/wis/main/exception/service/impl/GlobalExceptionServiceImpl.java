package com.wis.main.exception.service.impl;

import com.wis.main.configuration.Payload;
import com.wis.main.exception.ErrorResponse;
import com.wis.main.exception.service.GlobalExceptionService;
import com.wis.main.executation.CoreActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class GlobalExceptionServiceImpl extends CoreActionService<ErrorResponse,ErrorResponse,ErrorResponse> implements GlobalExceptionService {
    @Override
    protected ErrorResponse verify(Payload payload, ErrorResponse errorResponse, LocalDateTime now) {
        return errorResponse;
    }

    @Override
    protected ErrorResponse innerExecute(Payload payload, ErrorResponse errorResponse, LocalDateTime now) {
        return errorResponse;
    }
}
