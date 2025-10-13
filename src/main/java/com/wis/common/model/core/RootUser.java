package com.wis.common.model.core;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.wis.common.util.core_util.Constant;
import com.wis.common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RootUser {
    private String userId;
    private String username;
    private String phone;
    private String email;
    private String role;
    private String countryCode;

    public static RootUser fromHeader(HttpServletRequest request) {
        String userId      = request.getHeader(Constant.HEADER_JWT_USER_ID);
        String username    = request.getHeader(Constant.HEADER_JWT_USERNAME);
        String phone       = request.getHeader(Constant.HEADER_JWT_PHONE);
        String email       = request.getHeader(Constant.HEADER_JWT_EMAIL);
        String role        = request.getHeader(Constant.HEADER_JWT_ROLE);
        String countryCode = request.getHeader(Constant.HEADER_JWT_COUNTRY_CODE);

        if (StringUtil.isNullOrEmpty(userId)
                || StringUtil.isNullOrEmpty(username)
                || StringUtil.isNullOrEmpty(phone)
                || StringUtil.isNullOrEmpty(email)
                || StringUtil.isNullOrEmpty(role)
                || StringUtil.isNullOrEmpty(countryCode)) {

            throw ServiceException.of(HttpStatus.UNAUTHORIZED, "MISSING_AUTHORITY");
        }

        return RootUser.builder()
                .countryCode(countryCode)
                .userId(userId)
                .username(username)
                .email(email)
                .phone(phone)
                .role(role)
                .build();
    }
}

