package com.didichuxing.datachannel.arius.admin.biz.template.srv.limit;

import com.didichuxing.datachannel.arius.admin.client.bean.common.GetTemplateLimitStrategyContext;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateLimitStrategy;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

import java.util.List;

public interface TemplateLimitManager {

    /**
     * 获取模板管控策略
     * @param cluster  集群
     * @param template 模板
     * @param interval interval
     * @param context 计算上下文
     * @return result
     */
    TemplateLimitStrategy getTemplateLimitStrategy(String cluster, String template, long interval,
                                                   GetTemplateLimitStrategyContext context);

    /**
     * 调整模板的pipeline限流值
     * @param logicId 逻辑模板
     * @return true/false
     */
    boolean adjustPipelineRateLimit(Integer logicId);

    /**
    * 停止模板写入
    * @param physicalId 模板ID
    * @return result
    */
    Result<Void> stopIndexWrite(Long physicalId) throws ESOperateException;

    /**
     * 停止模板写入
     * @param physicalId 模板ID
     * @return result
     */
    Result<Void> startIndexWrite(Long physicalId) throws ESOperateException;

    /**
     * blockIndexWrite
     * @param cluster
     * @param indices
     * @param block
     * @return
     * @throws ESOperateException
     */
    Result<Void> blockIndexWrite(String cluster, List<String> indices, boolean block) throws ESOperateException;
}
