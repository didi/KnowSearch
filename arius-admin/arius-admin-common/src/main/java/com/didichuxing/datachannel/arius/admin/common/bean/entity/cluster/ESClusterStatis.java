package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.Data;

/**
 * 基本集群状态信息
 * @author wangshu
 * @date 2020/10/04
 */
@Data
public class ESClusterStatis extends BaseEntity {
    /**
     * 物理集群状态
     */
    private String status;

    /**
     * docNu
     */
    private double docNu;

    /**
     * 总的磁盘容量
     */
    private double totalDisk;

    /**
     * 已使用的磁盘容量
     */
    private double usedDisk;

    /**
     * 索引数量
     */
    private long indexNu;

    /**
     * 集群名称
     */
    private String name;

    /**
     * 集群id
     */
    private long id;
}
