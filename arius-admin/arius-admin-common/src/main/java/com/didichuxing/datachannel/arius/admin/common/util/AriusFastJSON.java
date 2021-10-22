package com.didichuxing.datachannel.arius.admin.common.util;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class AriusFastJSON {

    private static final ILog LOGGER = LogFactory.getLog(AriusFastJSON.class);

    public static <T> T parseObjectWithInit(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                LOGGER.error("method=toObjectWithInit||class={}||errMsg={}", clazz.getSimpleName(), e.getMessage());
            }
        }

        return JSON.parseObject(json, clazz);
    }
}
