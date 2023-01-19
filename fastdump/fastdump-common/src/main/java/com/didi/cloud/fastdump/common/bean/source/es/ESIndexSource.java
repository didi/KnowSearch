package com.didi.cloud.fastdump.common.bean.source.es;

import java.util.Map;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/23
 */
@Data
public class ESIndexSource extends BaseESSource {
    /***********************************源索引信息***************************************/
    /**
     * 源索引名称
     */
    private String                                         sourceIndex;

    /**
     * 源集群名称
     */
    private String                                         sourceCluster;

    /**
     * 源索引uuid
     */
    private String                                         uuid;

    /**
     * 源索引type, 8.x以下的es版本可能存在type
     */
    private String                                         sourceIndexType;

    /*********************************迁移索引shard信息***************************************/
    /**
     * 是否忽略索引健康度（red）
     */
    private Boolean                                        ignoreHealth = false;

    /**
     * key —> 或者副shard序号 ; value —> ip@dataPath
     */
    private Map<String/*shardNum*/, String/*ip@dataPath*/> shardNum2DataPathMap;

    /*********************************源索引所在集群信息***************************************/
    /**
     * 索引所有所在es集群ip:http-port,ip:http-port,ip:http-port
     */
    private String                                         sourceClusterAddress;

    /**
     * 支持的集群版本
     * @see com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum
     */
    private String                                         sourceClusterVersion;

    /**
     * 源es集群用户名
     */
    private String                                         sourceClusterUserName;

    /**
     * 源es集群密码
     */
    private String                                         sourceClusterPassword;
}
