package com.wis.enums;

import com.wis.exception.ServiceException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Getter
public enum Language {
    EN_US("en", "US"),
    VI_VN("vi", "VN"),
    JA_JP("ja", "JP");

    public static final Language DEFAULT_LANG = EN_US;

    private final Locale locale;

    Language(String language, String country) {
        this.locale = Locale.of(language, country);
    }

    public static Locale getDefault() {
        return EN_US.locale;
    }

    public static Language fromLangText(Locale locale) {
        if (locale == null) {
            return DEFAULT_LANG;
        }
        for (Language e : Language.values()) {
            if (e.getLocale().equals(locale)) {
                return e;
            }
        }
        throw ServiceException.of(HttpStatus.BAD_REQUEST, "ENUM_NOT_SUPPORTED");
    }

    public String getPropertiesFormat() {
        return this.getLocale().getDisplayLanguage();
    }

    public static Language fromLanguageCode(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return DEFAULT_LANG;
        }
        for (Language e : Language.values()) {
            if (e.getLocale().getLanguage().equalsIgnoreCase(langCode)) {
                return e;
            }
        }
        throw ServiceException.of(HttpStatus.BAD_REQUEST, "LANGUAGE_CODE_NOT_SUPPORTED");
    }

    public static List<Locale> supportedLocales() {
        return Arrays.stream(Language.values())
                .map(Language::getLocale)
                .toList();
    }

    public static boolean isSupported(Locale locale) {
        return Arrays.stream(Language.values())
                .anyMatch(e -> e.getLocale().equals(locale));
    }
}
