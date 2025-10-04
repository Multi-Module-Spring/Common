package com.wis.configuration;

import com.wis.enums.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Payload {
    private String token;
    private String apiUrl;
    private String apiCode;
    private Language language;
    private String requestId;
    private String env;
    private String clientId;
    private String clientSecret;
    private String ip;
    private String role;
    private String userId;
    private String username;
    private String countryCode;
    private String email;
    private long requestedAt;
}
