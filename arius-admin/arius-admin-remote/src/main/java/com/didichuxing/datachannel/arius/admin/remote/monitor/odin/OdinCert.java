package com.didichuxing.datachannel.arius.admin.remote.monitor.odin;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author limeng
 * @date 20/5/18
 */
public class OdinCert {

    private OdinCert() { throw new IllegalStateException("Utility class"); }

    /**
     * Token 有效时间 300 s
     */
    private static final Long TOKEN_TIME_SPAN = 300L;

    // 生成auth头
    public static Map<String, String> buildAuthorization(String sys, String skey) { // sys为系统名, skey为sys的秘钥
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", genAuth(sys, skey));
        return headers;
    }

    private static String genAuth(String callerName, String skey) {
        return new StringBuilder()
                .append("Cert caller=")
                .append(callerName)
                .append(",token=")
                .append(genToken(callerName, skey))
                .toString();
    }

    private static String genToken(String callerName, String skey) {
        long currentTs = System.currentTimeMillis() / 1000;
        long tknTs = currentTs - currentTs % TOKEN_TIME_SPAN;
        return DigestUtils.md5Hex("" + tknTs + "." + callerName + "." + skey);
    }
}