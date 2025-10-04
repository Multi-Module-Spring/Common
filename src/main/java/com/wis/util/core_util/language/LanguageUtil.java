package com.wis.util.core_util.language;

import com.wis.enums.Language;
import jakarta.servlet.http.HttpServletRequest;

public interface LanguageUtil {
    Language from(HttpServletRequest request);

    Language from();
}
