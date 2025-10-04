package com.wis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum Country {
    DEFAULT("NO_COUNTRY",0),
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
        throw new IllegalArgumentException("Unknown country code: " + code);
    }
}
