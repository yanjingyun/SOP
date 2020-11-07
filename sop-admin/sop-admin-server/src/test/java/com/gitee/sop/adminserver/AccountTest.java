package com.gitee.sop.adminserver;

import junit.framework.TestCase;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

/**
 * @author tanghc
 */
public class AccountTest  extends TestCase {

    /*
    生成密码
     */
    @Test
    public void genPwd() {
        String username = "admin";
        String password = "123456";
        String save_to_db = DigestUtils.md5Hex(username + DigestUtils.md5Hex(password) + username);
        System.out.println(save_to_db);
    }

}
