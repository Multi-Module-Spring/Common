package com.wis.util.message;

import java.util.List;
import java.util.Locale;


public interface MessageUtil {

    String getI18n(String key);

    String getI18n(String key, List<Object> args);

    String getI18n(String key, String detail);

    String getI18n(String key, String detail, List<Object> args);

}
