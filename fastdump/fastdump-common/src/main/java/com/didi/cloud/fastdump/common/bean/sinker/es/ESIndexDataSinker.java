package com.didi.cloud.fastdump.common.bean.sinker.es;

import com.didi.cloud.fastdump.common.enums.IndexModeEnum;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/24
 */
@Data
public class ESIndexDataSinker extends BaseESDataSinker {
    /***********************************sinker模式（索引模式）***************************************/
    /**
     * 源索引名称
     */
    private String sourceIndex;

    /**
     * @see com.didi.cloud.fastdump.common.enums.IndexModeEnum
     */
    private String indexMode = IndexModeEnum.CREATE.getMode();
    /***********************************目标索引信息***************************************/
    /**
     * 目标索引名称
     */
    private String targetIndex;

    /**
     * 源索引type, 7.x以下的es版本可能存在type
     */
    private String targetIndexType;

    /*********************************目标索引所在集群信息***************************************/
    /**
     * 索引所有所在集群ip:http-port,ip:http-port,ip:http-port
     */
    private String targetClusterAddress;

    /**
     * 目标集群名称
     */
    private String targetCluster;

    /**
     * 集群版本
     * @see com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum
     */
    private String targetClusterVersion;

    /**
     * 目标es集群用户名
     */
    private String targetClusterUserName;

    /**
     * 目标es集群密码
     */
    private String targetClusterPassword;
}
