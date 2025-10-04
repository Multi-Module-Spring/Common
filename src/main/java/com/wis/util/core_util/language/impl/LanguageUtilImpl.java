package com.wis.util.core_util.language.impl;

import com.wis.enums.Language;
import com.wis.util.core_util.language.LanguageUtil;
import com.wis.util.core_util.language.CustomLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguageUtilImpl implements LanguageUtil {

    @Autowired
    private CustomLocaleResolver resolver;

    @Override
    public Language from(HttpServletRequest request) {
        return Language.fromLangText(resolver.resolveLocale(request));
    }
}
