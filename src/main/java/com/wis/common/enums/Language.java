package com.wis.common.enums;

import com.wis.common.exception.ServiceException;
import com.wis.i18n.TranslateCommon;
import com.wis.i18n.exception.TranslateCommonException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Language {
    EN_US("en", "US"),
    VI_VN("vi", "VN"),
    JA_JP("ja", "JP");

    public static final Language DEFAULT_LANG = EN_US;

    private final String language;
    private final String country;

    Language(String language, String country) {
        this.language = language;
        this.country = country;
    }

    // ------------------ HÀM TIỆN ÍCH ------------------

    public static Language getDefault() {
        return DEFAULT_LANG;
    }

    public static Language fromLanguageCode(String code) {
        if (code == null || code.isBlank()) {
            return DEFAULT_LANG;
        }
        return Arrays.stream(Language.values())
                .filter(e -> e.language.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new TranslateCommonException(HttpStatus.BAD_REQUEST, TranslateCommon.LANGUAGE_CODE_NOT_SUPPORTED));
    }

    public static Language fromCountryCode(String code) {
        if (code == null || code.isBlank()) {
            return DEFAULT_LANG;
        }
        return Arrays.stream(Language.values())
                .filter(e -> e.country.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new TranslateCommonException(HttpStatus.BAD_REQUEST, TranslateCommon.LANGUAGE_CODE_NOT_SUPPORTED));
    }

    public static Language fromCode(String code) {
        if (code == null || code.isBlank()) {
            return DEFAULT_LANG;
        }
        String normalized = code.replace("_", "-");
        return Arrays.stream(Language.values())
                .filter(e -> (e.language + "-" + e.country).equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() ->
                        new TranslateCommonException(HttpStatus.BAD_REQUEST, TranslateCommon.LANGUAGE_CODE_NOT_SUPPORTED));
    }

    public static List<String> supportedCodes() {
        return Arrays.stream(Language.values())
                .map(Language::toCode)
                .toList();
    }

    public static boolean isSupported(String code) {
        try {
            fromCode(code);
            return true;
        } catch (ServiceException e) {
            return false;
        }
    }

    public String toCode() {
        return language + "_" + country;
    }
}
