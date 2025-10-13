package com.wis.common.util.core_util.language;

import com.wis.common.enums.Language;
import jakarta.servlet.http.HttpServletRequest;

public interface LanguageUtil {
    Language from(HttpServletRequest request);

    Language from();
}
