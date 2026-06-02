package cn.ggsn.openrxlight;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import okhttp3.MediaType;

public class Constants {
    public static final String COMPLETE_CHUNK = "chat.completion.chunk";
    public static final String CHAT_CALLBACK = "chat.callback";
    public static final String CHAT_HYBRID = "chat.hybrid";
    public static final String COMPLETE_NOTIFY = "chat.completion.notify";
    public static final String BASE_URL = "openrxlight.ggsn.cn";
    public static final String SCHEME = "https";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String EVENT_STREAM = "text/event-stream";

    public static final String RSA_KEY = "RSA";
    public static final String SM2_KEY = "SM2";
    public static final String HMAC_KEY = "HMAC";
    public static final String RSA_SHA256 = "SHA256withRSA";
    public static final String SM2_SM3 = "SM3withSM2";
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String GCM = "GCM";
    public static final String RSA_MD5 = "MD5withRSA";

    public static final String NO_PADDING = "NoPadding";
    public static final BigDecimal FEE_UNIT = BigDecimal.valueOf(100);

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter QUERY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter QUERY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter TIME_WITH_ZONE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm Z");
    public static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static final String STREAMING_PRINT_TEMPLATE_ID = "streaming_print";
    public static final String DEFAULT_PRINT_TEMPLATE_ID = "default_print";

}
