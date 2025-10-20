package com.wis.main.enums;

import com.wis.i18n.TranslateCommon;
import com.wis.i18n.exception.TranslateCommonException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum Country {
    UNKNOWN("NO_COUNTRY",0),
    US("United States", 1),
    VN("Vietnam", 2),
    JP("Japan", 3);

    private final String name;
    private final Integer code;

    public static String fromCode(Integer code) {
        for (Country country : values()) {
            if (Objects.equals(country.code, code)) {
                return country.getName();
            }
        }
        throw new TranslateCommonException(HttpStatus.NOT_FOUND, TranslateCommon.NOT_FOUND_COUNTRY);
    }
}
