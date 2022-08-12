package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatusWithTemplateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 该类用于表示集群连接的状态
 *
 * @author shizeying
 * @date 2022/08/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterConnectionStatusWithTemplateVO {
    private String status;
    private String desc;
    
    public ClusterConnectionStatusWithTemplateVO(ClusterConnectionStatusWithTemplateEnum clusterConnectionStatusWithTemplateEnum) {
        this.status = clusterConnectionStatusWithTemplateEnum.toString();
        this.desc = clusterConnectionStatusWithTemplateEnum.getDesc();
    }
}