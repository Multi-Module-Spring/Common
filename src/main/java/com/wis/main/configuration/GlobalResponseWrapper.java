package com.wis.main.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wis.main.exception.model.ErrorResponse;
import com.wis.main.model.core.ApiResponse;
import com.wis.main.util.core_util.paging.PagingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Component
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    private final PagingContext pagingContext;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType
            , MediaType selectedContentType
            , Class<? extends HttpMessageConverter<?>> selectedConverterType
            , ServerHttpRequest request
            , ServerHttpResponse response
    ) {
        boolean isPaging = pagingContext.get() != null;

        if (body instanceof ApiResponse) {
            ((ApiResponse<?>) body).setPaging(isPaging);
            return body;
        }

        if (body instanceof String) {
            try {
                return new ObjectMapper().writeValueAsString(
                        ApiResponse.builder()
                                .code(200)
                                .message("success")
                                .isPaging(isPaging)
                                .currentPage(pagingContext.get() != null ?pagingContext.get().getPage() : 0)
                                .totalCount(pagingContext.getTotalCount())
                                .totalPages(pagingContext.getTotalPages())
                                .data(body)
                                .build()
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        if(body instanceof ErrorResponse){
            return body;
        }

        return ApiResponse.builder()
                .code(200)
                .message("success")
                .isPaging(isPaging)
                .currentPage(pagingContext.get() != null ?pagingContext.get().getPage() : 0)
                .totalCount(pagingContext.getTotalCount())
                .totalPages(pagingContext.getTotalPages())
                .data(body)
                .build();
    }
}

