package com.didiglobal.logi.op.manager.infrastructure.util;

import org.springframework.web.client.RestTemplate;

/**
 * @author didi
 * @date 2022-08-31 12:00
 */
public class HttpUtil {

    private static RestTemplate restTemplate = new RestTemplate();

    private void HttpUtil() {
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
