package com.wis.util.message;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;


public interface MessageUtil {

    String getI18n(String key);

    String getI18n(String key, List<Object> args);

    String getI18n(String key, String detail);

    String getI18n(String key, String detail, List<Object> args);

    void importExcel(InputStream is, String filename);

    Workbook exportDynamicKeys();
}
