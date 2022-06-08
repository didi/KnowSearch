package com.didi.arius.gateway.util;

import com.didi.arius.gateway.common.metadata.AppDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuxuan
 * @Date 2022/6/6
 */
public class CustomDataSource {
    public static final String PHY_CLUSTER_NAME = "gateway_test_1";
    public static final int appid = 1;
    public static final String ip = "127.0.0.0" ;
    public static AppDetail appDetailFactory() {
        AppDetail appDetail = new AppDetail();
        appDetail.setId(appid);
        appDetail.setCluster(PHY_CLUSTER_NAME);
        List<String> ips = new ArrayList<String>();
        ips.add(ip);
        appDetail.setIp(ips);
        return appDetail;
    }
}
