package com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
public interface PreCreateManager {

    /**
     * 索引预先创建
     *
     * @param logicTemplateId 逻辑模板id
     * @return Result 创建是否成功
     */
    Result<Boolean> preCreateIndex(Integer logicTemplateId) throws ESOperateException;

    /**
     * 异步创建今明天索引
     * @param physicalId 物理模板id
     */
    void asyncCreateTodayAndTomorrowIndexByPhysicalId(Long physicalId);

    /**
     * 同步创建今天索引
     *
     * @param physicalId 物理模版id
     * @param version    版本
     * @return boolean
     * @throws ESOperateException esoperate例外
     */
    boolean syncCreateTodayIndexByPhysicalId(Long physicalId, int version) throws ESOperateException;

        ////////////////////srv
    /**
    * 索引预先创建
    * @param cluster 集群
    * @param retryCount 重试次数
    * @return true/false
    * @throws AdminOperateException
    */
    @Deprecated
    boolean preCreateIndex(String cluster, int retryCount);

    /**
     * 重建明天索引
     * @param logicId 逻辑模板id
     * @param retryCount 重试次数
     * @return true/false
     */
    boolean reBuildTomorrowIndex(Integer logicId, int retryCount) throws ESOperateException;
}