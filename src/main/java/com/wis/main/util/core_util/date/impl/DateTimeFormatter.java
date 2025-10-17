package com.wis.main.util.core_util.date.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class DateTimeFormatter implements com.wis.main.util.core_util.date.DateTimeFormatter {
    private static final Map<String, java.time.format.DateTimeFormatter> formatters = new ConcurrentHashMap<>();

    @Override
    public java.time.format.DateTimeFormatter get(String pattern) {
        if (pattern == null) {
            return null;
        }
        pattern = pattern.trim();
        if (pattern.isEmpty()) {
            return null;
        }
        if (!formatters.containsKey(pattern)) {
            formatters.put(pattern, java.time.format.DateTimeFormatter.ofPattern(pattern));
        }
        return formatters.get(pattern);
    }
}
