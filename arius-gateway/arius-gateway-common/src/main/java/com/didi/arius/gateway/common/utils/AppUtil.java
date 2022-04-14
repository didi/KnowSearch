package com.didi.arius.gateway.common.utils;

import com.didi.arius.gateway.common.metadata.AppDetail;

public class AppUtil {

    private AppUtil(){}

    public static boolean isAdminAppid(AppDetail appDetail) {
        return (appDetail != null) && appDetail.getIsRoot() == 1;
    }
}
