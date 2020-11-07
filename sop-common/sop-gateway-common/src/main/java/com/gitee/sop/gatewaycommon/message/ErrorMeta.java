package com.gitee.sop.gatewaycommon.message;

import com.gitee.sop.gatewaycommon.exception.ApiException;
import lombok.Getter;

import java.util.Locale;

/**
 * 错误对象
 *
 * @author tanghc
 */
@Getter
public class ErrorMeta {

    private static final Locale ZH_CN = Locale.SIMPLIFIED_CHINESE;

    private String modulePrefix;
    private String code;
    private String subCode;

    public ErrorMeta(String modulePrefix, String code, String subCode) {
        this.modulePrefix = modulePrefix;
        this.code = code;
        this.subCode = subCode;
    }

    public Error getError(Locale locale) {
        return ErrorFactory.getError(this, locale);
    }

    /**
     * 返回网关exception
     *
     * @param params 参数
     * @return 返回exception
     */
    public ApiException getException(Object... params) {
        if (params != null && params.length == 1) {
            Object param = params[0];
            if (param instanceof Throwable) {
                return new ApiException((Throwable) param, this);
            }
        }
        return new ApiException(this, params);
    }

    @Override
    public String toString() {
        return modulePrefix + code + "_" + subCode;
    }
}
