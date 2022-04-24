package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterMonitorTask {
    /**
     * 主键id
     */
    private Long        id;
    /**
     * 集群id
     */
    private Integer     clusterId;
    /**
     * 集群名称
     */
    private String      cluster;
    /**
     * 集群数据中心：us01/cn
     */
    private String      dataCentre;
    /**
     * motitor集群host
     */
    private String      monitorHost;
    /**
     * motitor监控时间
     */
    private Date        monitorTime;
    /**
     * 创建时间
     */
    private Date        gmtCreate;
    /**
     * 更新时间
     */
    private Date        gmtModify;
    /**
     * 修改记录使用,monitorHost字段用于校验是否被修改过
     */
    private String      destMonitorHost;
}
