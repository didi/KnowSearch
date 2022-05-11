package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
public interface IndexPlanManager {

    /**
     * indexRollover能力
     * 获取当天的索引主分片占用磁盘容量大小
     * 如果小于主shardCnt*30G，则不进行升级版本
     * 如果大于主shardCnt*50G，则直接升级版本
     * 如果超过了过去7天索引大小的最大值，则升级版本
     * @param logicTemplateId 逻辑模板id
     * @return boolean
     */
    Result<Void> indexRollover(Integer logicTemplateId);

    /**
     * 调整索引模版的shard个数配置
     * @param logicTemplateId 逻辑模板id
     * @return boolean 是否成功
     */
    Result<Void> adjustShardCountByPhyClusterName(Integer logicTemplateId);

    /**
     * 根据shard设置ShardRouting，再把shard调整到合适大小
     * @param param IndexTemplatePhyDTO
     */
    void initShardRoutingAndAdjustShard(IndexTemplatePhyDTO param);
}
