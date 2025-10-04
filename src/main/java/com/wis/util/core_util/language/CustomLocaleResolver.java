package com.wis.util.core_util.language;

import com.wis.enums.Language;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class CustomLocaleResolver extends AcceptHeaderLocaleResolver {

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.stream(Language.values())
            .map(Language::getLocale)
            .toList();

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLang = request.getHeader("Accept-Language");
        if (headerLang == null || headerLang.isBlank()) {
            return Language.getDefault();
        }

        // Replace "_" with "-" to ensure proper parsing
        headerLang = headerLang.replace('_', '-');

        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerLang);
        Locale locale = Locale.lookup(list, SUPPORTED_LOCALES);

        return locale != null ? locale : Language.getDefault();
    }
}

