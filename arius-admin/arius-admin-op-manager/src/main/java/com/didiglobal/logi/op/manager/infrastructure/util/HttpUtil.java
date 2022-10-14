package com.didiglobal.logi.op.manager.infrastructure.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author didi
 * @date 2022-08-31 12:00
 */
public class HttpUtil {

    private static RestTemplate restTemplate;

    private void HttpUtil() {
    }

    public synchronized static RestTemplate getRestTemplate() {
        if (null == restTemplate) {
            SimpleClientHttpRequestFactory clientHttpRequestFactory
                    = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(10 * 1000);
            clientHttpRequestFactory.setReadTimeout(10 * 1000);
            restTemplate = new RestTemplate(clientHttpRequestFactory);
        }
        return restTemplate;
    }
}
