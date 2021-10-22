package com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

public interface TemplatePreCreateManager {

    /**
     * 索引预先创建
     * @param cluster 集群
     * @param retryCount 重试次数
     * @return true/false
     * @throws AdminOperateException
     */
    boolean preCreateIndex(String cluster, int retryCount);

    /**
     * 重建明天索引
     * @param logicId 逻辑模板id
     * @param retryCount 重试次数
     * @return true/false
     */
    boolean reBuildTomorrowIndex(Integer logicId, int retryCount) throws ESOperateException;

    /**
     * 异步创建今明天索引
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     */
    void asyncCreateTodayAndTomorrowIndexByPhysicalId(Long physicalId, int retryCount);
}
