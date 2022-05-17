package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.adjustshard;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public interface AdjustShardManager {

    /**
     * 执行调整shard 数量
     * @param logicTemplateId 模板id
     * @param shardNum 调整后的shard数量
     * @return 调整结果
     */
    Result<Void> adjustShard(Integer logicTemplateId, Integer shardNum);
}
