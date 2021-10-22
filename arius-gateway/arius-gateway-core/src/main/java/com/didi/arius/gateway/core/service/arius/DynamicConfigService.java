package com.didi.arius.gateway.core.service.arius;

public interface DynamicConfigService {

    /**
     * 获取detailLogFlag
     * @return
     */
    boolean getDetailLogFlag();

    /**
     * 是否可以跳过检查
     *
     * @param appid
     * @return
     */
    boolean isWhiteAppid(int appid);

    /**
     * 从admin拉取gateway动态配置信息，并更新
     */
    void resetDynamicConfigInfo();
}
