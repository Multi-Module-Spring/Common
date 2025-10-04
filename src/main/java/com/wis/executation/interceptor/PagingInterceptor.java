package com.wis.executation.interceptor;

import com.wis.model.core.PagingInfo;
import com.wis.util.core_util.CoreRepository;
import com.wis.util.core_util.paging.PagingContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class PagingInterceptor extends CoreRepository implements HandlerInterceptor {

    @Autowired
    private PagingContext pagingContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String pageStr = request.getParameter("page");
        String sizeStr = request.getParameter("pageSize");
        pagingContext.clear();
        if (pageStr != null && sizeStr != null) {
            try {
                int page = Integer.parseInt(pageStr);
                int size = Integer.parseInt(sizeStr);
                pagingContext.set(PagingInfo.builder()
                                .page(page)
                                .pageSize(size)
                        .build());
            } catch (NumberFormatException ignored) {}
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("[REQUEST] {} {} from IP {} -> status {}",
                request.getMethod(),
                request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                request.getRemoteAddr(),
                response.getStatus()
        );

        PagingInfo paging = pagingContext.get();
        if (paging != null) {
            log.info("[PAGING] page: {}, pageSize: {}, limit: {}, offset: {}",
                    paging.getPage(),
                    paging.getPageSize(),
                    paging.getLimit(),
                    paging.getOffset());
        } else {
            log.info("[PAGING] No paging applied");
        }
        pagingContext.clear();

        if (ex != null) {
            log.error("[ERROR] Exception occurred during request", ex);
        }
    }
}

