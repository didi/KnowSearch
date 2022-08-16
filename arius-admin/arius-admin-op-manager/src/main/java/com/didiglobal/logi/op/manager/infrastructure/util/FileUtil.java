package com.didiglobal.logi.op.manager.infrastructure.util;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.SLASH;
import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.UNDER_SCORE;

/**
 * @author didi
 * @date 2022-08-15 14:08
 */
public class FileUtil {

    public static String getUniqueFileName(String name, String fileName) {
        return name + UNDER_SCORE + fileName;
    }

    public static String getDeleteFileName(String contentUrl) {
        return contentUrl.substring(contentUrl.lastIndexOf(SLASH) + 1);
    }
}
