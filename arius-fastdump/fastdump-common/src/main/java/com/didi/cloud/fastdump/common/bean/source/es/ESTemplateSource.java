package com.didi.cloud.fastdump.common.bean.source.es;

import com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum;
import lombok.Data;

import java.util.List;

/**
 * Created by linyunan on 2022/8/23
 */
@Data
public class ESTemplateSource extends BaseESSource {
    /***********************************源索引信息***************************************/
    /**
     * 源模板名称
     */
    private String       sourceTemplate;

    /**
     * 源索引名称列表
     */
    private List<String> sourceIndices;

    /**
     * 源集群名称
     */
    private String       sourceCluster;

    /**
     * 是否需要忽略模板下的索引状态
     */
    private Boolean      ignoreHealth = false;
    /*********************************源索引所在集群信息***************************************/
    /**
     * 索引所有所在es集群ip:http-port,ip:http-port,ip:http-port
     */
    private String       sourceClusterAddress;

    /**
     * 支持的集群版本
     * @see ESClusterVersionEnum
     */
    private String       sourceClusterVersion;

    /**
     * 源es集群用户名
     */
    private String       sourceClusterUserName;

    /**
     * 源es集群密码
     */
    private String       sourceClusterPassword;
}
