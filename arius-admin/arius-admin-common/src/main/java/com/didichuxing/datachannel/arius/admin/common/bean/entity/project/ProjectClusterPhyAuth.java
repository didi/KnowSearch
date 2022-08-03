package com.didichuxing.datachannel.arius.admin.common.bean.entity.project;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectClusterPhyAuth extends BaseEntity {
    /**
     * ID
     */
    private Long    id;

    /**
     * 应用ID
     */
    private Integer projectId;

    /**
     * 物理集群名称
     */
    private String  clusterPhyName;

    /**
     * 权限类型  读写  读
     * @see ProjectClusterLogicAuthEnum
     */
    private Integer type;

}