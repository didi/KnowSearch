package com.didichuxing.datachannel.arius.admin.biz.template.srv.shard;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

public interface TemplateShardManager {

    /**
     * adjustShardCount
     * @param cluster
     * @param retryCount
     * @return
     */
    boolean adjustShardCount(String cluster, int retryCount);

    /**
     * shard个数调整
     * @param physicalId physicalId
     * @param retryCount retryCount
     * @return result
     */
    Result adjustShardCount(Long physicalId, int retryCount) throws ESOperateException;

    /**
     * 计算shard个数
     * @param physical
     * @return
     */
    int calcuShardCount(Long physical);

    /**
     * 更新shard
     * @param physicalId id
     * @param shardNum num
     * @return result
     */
    Result updateTemplateShardNumIfGreater(Long physicalId, Integer shardNum, int retryCount) throws ESOperateException;

    /**
     * initShardRoutingAndAdjustShard
     * @param param
     */
    void initShardRoutingAndAdjustShard(IndexTemplatePhysicalDTO param);
}
