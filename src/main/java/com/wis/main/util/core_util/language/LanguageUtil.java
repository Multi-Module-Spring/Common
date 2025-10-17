package com.wis.main.util.core_util.language;

import com.wis.main.enums.Language;
import jakarta.servlet.http.HttpServletRequest;

public interface LanguageUtil {
    Language from(HttpServletRequest request);

    Language from();
}
