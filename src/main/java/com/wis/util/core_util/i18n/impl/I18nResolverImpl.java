package com.wis.util.core_util.i18n.impl;

import com.wis.exception.ServiceException;
import com.wis.annotation.I18n;
import com.wis.util.core_util.i18n.I18nResolver;
import com.wis.util.message.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class I18nResolverImpl implements I18nResolver {

    private final MessageUtil messageUtil;

    public I18nResolverImpl(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @Override

    public void resolveI18nFields(Object dto) {
        if (dto == null) return;

        if (dto instanceof List<?> list) {
            for (Object item : list) {
                resolveI18nFields(item);
            }
            return;
        }

        Class<?> clazz = dto.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || field.isSynthetic()) continue;
            if ("serialVersionUID".equals(field.getName())) continue;

            field.setAccessible(true);

            try {
                Object value = field.get(dto);
                if (value == null) continue;

                I18n i18nField = field.getAnnotation(I18n.class);
                if (i18nField != null && value instanceof String stringValue) {
                    List<Object> args = Arrays.stream(i18nField.args())
                            .map(argKey -> {
                                try {
                                    if (argKey.startsWith("__")) {
                                        return argKey.substring(2).replace("_", " ");
                                    }
                                    Field argField = clazz.getDeclaredField(argKey);
                                    argField.setAccessible(true);
                                    return argField.get(dto);
                                } catch (Exception e) {
                                   log.info("error {}",e.getMessage());
                                }
                                return null;
                            })
                            .toList();

                    String i18nValue = messageUtil.getI18n(stringValue, args);
                    field.set(dto, i18nValue);
                    continue;
                }


                if (isCustomObject(field.getType())) {
                    resolveI18nFields(value);
                }

                if (value instanceof List<?> list) {
                    for (Object item : list) {
                        resolveI18nFields(item);
                    }
                }

            } catch (Exception e) {
                log.info("error {}",e.getMessage());
            }
        }
    }



    private boolean isCustomObject(Class<?> clazz) {
        return !(clazz.isPrimitive()
                || clazz.isEnum()
                || clazz.equals(String.class)
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class.equals(clazz)
                || clazz.getPackageName().startsWith("java."));
    }


}

