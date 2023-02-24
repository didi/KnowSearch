package com.didi.cloud.fastdump.common.bean.sinker.es;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/24
 */
@Data
public class ESTemplateDataSinker extends BaseESDataSinker {
    /***********************************sinker模式（索引模式）***************************************/
    /**
     * 源模板名称
     */
    private String  sourceTemplate;

    /**
     * 写入速度  条/s
     */
    private Long globalReadFileRateLimit = 10000000000L;

    /***********************************目标索引信息***************************************/
    /**
     * 目标模板名称
     */
    private String  targetTemplate;

    /*********************************目标索引所在集群信息***************************************/
    /**
     * 索引所有所在集群ip:http-port,ip:http-port,ip:http-port
     */
    private String  targetClusterAddress;

    /**
     * 目标集群名称
     */
    private String  targetCluster;

    /**
     * 集群版本
     * @see com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum
     */
    private String  targetClusterVersion;

    /**
     * 目标es集群用户名
     */
    private String  targetClusterUserName;

    /**
     * 目标es集群密码
     */
    private String  targetClusterPassword;
}
