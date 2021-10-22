package com.didi.arius.gateway.common.utils;

import com.didi.arius.gateway.common.metadata.AppDetail;

public class AppUtil {

    public static boolean isAdminAppid(AppDetail appDetail) {
        return appDetail == null ? false : appDetail.getIsRoot() == 1;
    }
}
