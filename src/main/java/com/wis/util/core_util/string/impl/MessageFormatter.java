package com.wis.util.core_util.string.impl;

import com.wis.util.core_util.string.Formatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class MessageFormatter implements Formatter {
    private static final Map<String, MessageFormat> formatters = new ConcurrentHashMap<>();

    @Override
    public MessageFormat get(String pattern) {
        if (pattern == null) {
            return null;
        }
        pattern = pattern.trim();
        if (pattern.isEmpty()) {
            return null;
        }
        if (!formatters.containsKey(pattern)) {
            formatters.put(pattern, new MessageFormat(pattern));
        }
        return formatters.get(pattern);
    }
}
