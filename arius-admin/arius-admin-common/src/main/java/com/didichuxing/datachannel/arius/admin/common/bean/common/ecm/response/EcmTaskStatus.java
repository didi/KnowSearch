package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response;

import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmHostStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * ES集群状态信息
 * @author didi
 * @date 2020/10/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcmTaskStatus implements Serializable {
    /**
     * 主机名或IP
     */
    private String            hostname;

    /**
     * 容器云/物理机 接口返回任务ID
     */
    private Integer           taskId;

    /**
     * pod分组
     */
    private Integer           group;

    /**
     * pod index
     */
    private Integer           podIndex;

    /**
     * pod IP
     */
    private String            podIp;

    /**
     * 主机状态
     */
    private EcmHostStatusEnum statusEnum;

    /**
     * 创建时间
     */
    private Date              createTime;

    /**
     * 修改时间
     */
    private Date              updateTime;
}
