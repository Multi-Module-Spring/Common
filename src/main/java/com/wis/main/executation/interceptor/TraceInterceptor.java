package com.wis.main.executation.interceptor;

import com.wis.main.util.RequestTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@Slf4j
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestTrace.clear();
        RequestTrace.add("[API_CALL] " + request.getMethod() + " " + request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        List<String> trace = RequestTrace.get();

        log.info("[REQUEST_TRACE] API {} {} from IP {} -> status {}",
                request.getMethod(),
                request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                request.getRemoteAddr(),
                response.getStatus()
        );

        trace.forEach(info -> log.info("[TRACE_STEP] {}", info));

        if (ex != null) {
            log.error("[TRACE_ERROR] Exception occurred during request", ex);
        }

        RequestTrace.clear();
    }
}
