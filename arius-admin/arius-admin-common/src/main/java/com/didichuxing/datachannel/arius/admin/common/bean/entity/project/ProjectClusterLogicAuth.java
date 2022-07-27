package com.didichuxing.datachannel.arius.admin.common.bean.entity.project;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectClusterLogicAuth extends BaseEntity {
    /**
     * ID
     */
    private Long    id;

    /**
     * 应用ID
     */
    private Integer projectId;

    /**
     * 逻辑集群ID
     */
    private Long    logicClusterId;

    /**
     * 权限类型  管理、访问、无权限
     * @see ProjectClusterLogicAuthEnum
     */
    private Integer type;


}