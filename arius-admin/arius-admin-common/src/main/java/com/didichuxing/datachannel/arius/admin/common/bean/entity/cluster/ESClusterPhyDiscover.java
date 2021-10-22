package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ES物理集群Kibana Discover
 * @author wangshu
 * @date 2020/09/10
 */
@Data
public class ESClusterPhyDiscover implements Serializable {
    /**
     * 物理集群名称
     */
    private String cluster;

    /**
     * 关联索引表达式
     */
    private String indices;

    /**
     * discover名称
     */
    private String discoverName;

    /**
     * discover描述
     */
    private String discoverDesc;

    /**
     * Kibana Discover URL
     */
    private String discoverUrl;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date creationTime;

    /**
     * 最后更新时间
     */
    private Date lastModifyTime;
}
