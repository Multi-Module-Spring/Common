package com.wis.configuration;

import com.wis.enums.Language;
import com.wis.util.core_util.language.CustomLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        CustomLocaleResolver resolver = new CustomLocaleResolver();
        resolver.setDefaultLocale(Language.getDefault());
        resolver.setSupportedLocales(Arrays.stream(Language.values())
                .map(Language::getLocale)
                .toList());
        return resolver;
    }
}


