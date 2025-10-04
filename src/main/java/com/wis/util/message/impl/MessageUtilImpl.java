package com.wis.util.message.impl;

import com.wis.enums.Language;
import com.wis.exception.ServiceException;
import com.wis.util.message.MessageUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

@Component
@Slf4j
public class MessageUtilImpl implements MessageUtil {

    public String getI18n(String key) {
        return getI18n(key,"Error occurred", List.of());
    }

    public String getI18n(String key, List<Object> args) {
        return getI18n(key,"Error occurred", args);
    }

    public String getI18n(String key,String detail) {
        return getI18n(key,detail, List.of());
    }

    public String getI18n(String key,String detail, List<Object> args) {
        Locale locale = LocaleContextHolder.getLocale();
        return getI18n(locale, key,detail, args);
    }

//    public String getI18n(String bundle, Locale locale, String key,String detail, List<Object> args) {
//        try {
//            ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, locale);
//            String pattern = resourceBundle.getString(key);
//            return MessageFormat.format(pattern, args != null ? args.toArray() : null);
//        } catch (Exception e) {
//            log.error("Không tìm thấy key '{}' trong bundle '{}'", key, bundle, e);
//            return detail;
//        }
//    }

    private String getI18n(Locale locale, String key, String detail, List<Object> args) {
        Map<Locale, String> localeMap = translations.get(key);

        String message = (localeMap != null) ? localeMap.get(locale) : null;

        if (message == null) {
            log.warn("Không tìm thấy key '{}' cho locale '{}'", key, locale);
            throw ServiceException.of(HttpStatus.CONFLICT, "I18N_UNKNOWN", List.of(key,detail));
        }

        return MessageFormat.format(message, args != null ? args.toArray() : null);
    }

    private final Map<String, Map<Locale, String>> translations = new HashMap<>();

    @PostConstruct
    void init() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:i18n/*.xlsx");

            for (Resource resource : resources) {
                loadExcel(resource);
            }
        } catch (Exception e) {
            log.error("Lỗi load file excel i18n", e);
        }
    }

        private void loadExcel(Resource resource) {
            try (InputStream is = resource.getInputStream();
                 Workbook workbook = new XSSFWorkbook(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                Row header = sheet.getRow(0);

                Map<Integer, Language> columnLangMap = new HashMap<>();
                for (int col = 1; col < header.getLastCellNum(); col++) {
                    String langCode = header.getCell(col).getStringCellValue().toUpperCase(Locale.ROOT);
                    try {
                        Language lang = Language.valueOf(langCode);
                        columnLangMap.put(col, lang);
                    } catch (IllegalArgumentException ex) {
                        log.warn("Bỏ qua cột {} với header '{}' vì không khớp Enum Language", col, langCode);
                    }
                }

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String key = row.getCell(0).getStringCellValue();

                    for (Map.Entry<Integer, Language> entry : columnLangMap.entrySet()) {
                        int colIndex = entry.getKey();
                        Language lang = entry.getValue();

                        Cell cell = row.getCell(colIndex);
                        if (cell == null) continue;

                        String value = cell.getStringCellValue();
                        translations
                                .computeIfAbsent(key, k -> new HashMap<>())
                                .put(lang.getLocale(), value);
                    }
                }

                log.info("Đã load file i18n: {}", resource.getFilename());

        } catch (Exception e) {
                log.error("Lỗi đọc file i18n: {}", resource.getFilename(), e);
        }
    }



}
