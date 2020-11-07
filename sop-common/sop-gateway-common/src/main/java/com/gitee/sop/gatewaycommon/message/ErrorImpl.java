package com.gitee.sop.gatewaycommon.message;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class ErrorImpl implements Error {

    private String code;
    private String msg;
    private String sub_code;
    private String sub_msg;
    private String solution;

    public ErrorImpl(String code, String msg, String sub_code, String sub_msg, String solution) {
        this.code = code;
        this.msg = msg;
        this.sub_code = sub_code;
        this.sub_msg = sub_msg;
        this.solution = solution;
    }
}