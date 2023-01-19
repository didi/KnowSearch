package com.didiglobal.logi.op.manager.infrastructure.util;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SLASH;
import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.UNDER_SCORE;

/**
 * @author didi
 * @date 2022-08-15 14:08
 */
public class FileUtil {
    private static final ILog LOGGER   = LogFactory.getLog(FileUtil.class);
    public static final String STR_EMPTY = "";

    public static String getUniqueFileName(String name, String fileName) {
        return name + UNDER_SCORE + System.currentTimeMillis() + UNDER_SCORE + fileName;
    }

    public static String getDeleteFileName(String contentUrl) {
        try {
            String delFileName = contentUrl.substring(contentUrl.lastIndexOf(SLASH) + 1);
            return URLDecoder.decode(delFileName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("class=FileUtil||method=getDeleteFileName||errMsg={}", e.getMessage(), e);
            return STR_EMPTY;
        }
    }
}
