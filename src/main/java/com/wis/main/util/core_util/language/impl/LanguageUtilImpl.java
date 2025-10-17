package com.wis.main.util.core_util.language.impl;

import com.wis.main.enums.Language;
import com.wis.main.util.core_util.language.LanguageUtil;
import com.wis.main.util.core_util.language.CustomLanguageResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguageUtilImpl implements LanguageUtil {

    @Autowired
    private CustomLanguageResolver resolver;

    @Override
    public Language from(HttpServletRequest request) {
        return resolver.resolveLanguage(request);
    }

    @Override
    public Language from() {
        return resolver.resolveLanguage();
    }
}
