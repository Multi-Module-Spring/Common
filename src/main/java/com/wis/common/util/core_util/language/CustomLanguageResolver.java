package com.wis.common.util.core_util.language;

import com.wis.common.enums.Language;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CustomLanguageResolver {

    public Language resolveLanguage(HttpServletRequest request) {
            String headerLang = extractAcceptLanguage(request);
            if (headerLang == null || headerLang.isBlank()) {
                return Language.getDefault();
            }
            return Language.fromCode(headerLang);
    }

    public Language resolveLanguage() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return Language.getDefault();
        }
        return resolveLanguage(request);
    }

    private String extractAcceptLanguage(HttpServletRequest request) {
        if (request == null) return null;

        String headerLang = request.getHeader("Accept-Language");
        if (headerLang == null || headerLang.isBlank()) {
            return null;
        }
        return headerLang.split(",")[0].trim().replace('_', '-');
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }
}
