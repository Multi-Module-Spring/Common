package com.wis.common.util.core_util;

public class Constant {

    public static final String HEADER_TIMEZONE_IANA = "The-Timezone-IANA";
    public static final String HEADER_JWT_USER_ID     = "X-Authen-JWT-cth-user-id";
    public static final String HEADER_JWT_USERNAME    = "X-Authen-JWT-cth-username";
    public static final String HEADER_JWT_PHONE       = "X-Authen-JWT-cth-phone";
    public static final String HEADER_JWT_EMAIL       = "X-Authen-JWT-cth-email";
    public static final String HEADER_JWT_ROLE        = "X-Authen-JWT-cth-role";
    public static final String HEADER_JWT_COUNTRY_CODE = "X-Authen-JWT-cth-country-code";
    public static final String SIGN_METHOD_HMAC_SHA256 = "HmacSHA256";
    public static final String SIGN_METHOD_SHA256 = "sha256";
    public static final String SIGN_METHOD_HMAC = "hmac";

    public static String DATE_REQUEST_FORMATTER = "yyyy-MM-dd";
    public static String DATE_RESPONSE_FORMATTER = "dd/MM/yyyy";
    public static String EXCEL_FILENAME_DATE_FORMAT = "ddMMyyyy";

    public static String TIME_RESPONSE_FORMATTER = "HH:mm:ss";

    public static String DATE_FORMATTER = "dd/MM/yyyy HH:mm:ss";

    public static String DATE_TITLE_EXPORT_FORMATTER = "dd_MM_yyyy_HH_mm_ss";

    public static String PHONE_NUMBER_VN_REGEX = "\\d{9,12}";

    public static String YYYY_MM_DD_HH_MM_SS_SS = "yyyy-MM-dd'T'HH:mm:ss.SS";
    public static String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static String YYYY_MM_DD__HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    public static int MAXIMUM_RECORD_EXPORT = 5000;


    public static String PHONE_NUMBER_REGEX = "^(0|84|95|840|950|\\+0|\\+84|\\+840|\\+95||\\+950)[0-9]{8,10}$";

    public static String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    public static String SKU_SEARCH_PDA_REGEX = "^[A-Za-z0-9\\\\-_\\\\\\\\/&@]+$";

    public static Integer MAX_LENGTH_SEARCH = 200;
    public static Integer DEFAULT_PAGE_SIZE = 10;

    public static final String ISO_DATE_TIME_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String ISO_DATE_TIME_TIMEZONE_1 = "yyyy-MM-dd'T'HH:mm:ssXXX";
}
