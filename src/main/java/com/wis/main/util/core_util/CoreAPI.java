package com.wis.main.util.core_util;

import com.wis.main.configuration.Payload;
import com.wis.main.enums.Language;
import com.wis.main.model.core.RootUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public abstract class CoreAPI extends CoreBean {

    @Autowired
    protected HttpServletRequest request;

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(bearer) (.*)", Pattern.CASE_INSENSITIVE);

    protected Payload payload() {
        return payload(true);
    }

    protected Payload payload(boolean requiredToken) {
        String token = token();
        Language language = languageUtil.from(request);
        RootUser user = requiredToken ? RootUser.fromHeader(request) : null;

        return Payload.builder()
                .token(token)
                .language(language)
                .requestId(header("x-request-id"))
                .clientId(header("x-client-id"))
                .clientSecret(header("x-client-secret"))
                .env(header("x-env"))
                .role(stringUtil.nvl(user == null ? null : user.getRole()))
                .userId(stringUtil.nvl(user == null ? null : user.getUserId()))
                .username(stringUtil.nvl(user == null ? null : user.getUsername()))
                .countryCode(stringUtil.nvl(user == null ? null : user.getCountryCode()))
                .email(stringUtil.nvl(user == null ? null : user.getEmail()))
                .requestedAt(System.currentTimeMillis())
                .build();
    }

    protected String token() {
        String token = "";
        String authorization = stringUtil.nvl(header("Authorization"), header("x-access-token"));
        if (stringUtil.isEmpty(authorization)) {
            return token;
        }
        token = stringUtil.nvl(authorization);
        Matcher matcher = TOKEN_PATTERN.matcher(token);
        if (matcher.find()) {
            token = stringUtil.nvl(matcher.group(2));
        }
        return token;
    }

    protected String header(String key) {
        return request.getHeader(key);
    }
}

