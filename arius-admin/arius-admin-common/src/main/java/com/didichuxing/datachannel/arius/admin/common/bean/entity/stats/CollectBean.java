package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

public interface CollectBean {
    /**
     * 获取采集时间戳，毫秒
     * @return
     */
    long getTimestamp();

    /**
     * 获取采集的值
     * @return
     */
    Double getValue();
}
