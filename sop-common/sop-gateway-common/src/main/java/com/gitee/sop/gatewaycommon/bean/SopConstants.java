package com.gitee.sop.gatewaycommon.bean;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author tanghc
 */
public class SopConstants {

    private SopConstants() {}

    public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    public static final String UTF8 = "UTF-8";
    public static final String FORMAT_JSON = "json";
    public static final String DEFAULT_SIGN_METHOD = "md5";
    public static final String EMPTY_JSON = "{}";

    public static final String METADATA_SERVER_CONTEXT_PATH = "server.servlet.context-path";

    public static final String METADATA_SERVER_CONTEXT_PATH_COMPATIBILITY = "context-path";

    /**
     * 在拦截器中调用获取参数：
     * String cachedBody = (String)exchange.getAttribute(SopConstants.CACHE_REQUEST_BODY_OBJECT_KEY);
     */
    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    /**
     * 在拦截器中调用获取参数：
     * Map<String, String> params = exchange.getAttribute(SopConstants.CACHE_REQUEST_BODY_FOR_MAP);
     */
    public static final String CACHE_REQUEST_BODY_FOR_MAP = "cacheRequestBodyForMap";

    public static final String CACHE_API_PARAM = "cacheApiParam";

    public static final String CACHE_UPLOAD_REQUEST = "cacheUploadRequest";

    public static final String X_SERVICE_ERROR_CODE = "x-service-error-code";

    public static final String X_SERVICE_ERROR_MESSAGE = "x-service-error-message";

    public static final int BIZ_ERROR_STATUS = 4000;

    public static final String UNKNOWN_SERVICE= "_sop_unknown_service_";
    public static final String UNKNOWN_METHOD = "_sop_unknown_method_";
    public static final String UNKNOWN_VERSION = "_sop_unknown_version_";

    public static final String METADATA_ENV_KEY = "env";
    public static final String METADATA_ENV_PRE_VALUE = "pre";
    public static final String METADATA_ENV_GRAY_VALUE = "gray";

    public static final String CACHE_ROUTE_INTERCEPTOR_CONTEXT = "cacheRouteInterceptorContext";
    public static final String TARGET_SERVICE = "sop-target-service";
    public static final String RESTFUL_REQUEST = "sop-restful-request";

    public static final String METADATA_KEY_TIME_STARTUP = "server.startup-time";

    public static final String CACHE_ROUTE_INFO = "cacheRouteInfo";

}
