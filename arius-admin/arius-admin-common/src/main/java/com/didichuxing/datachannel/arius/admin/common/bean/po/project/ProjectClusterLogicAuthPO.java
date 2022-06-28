package com.didichuxing.datachannel.arius.admin.common.bean.po.project;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逻辑集群权限PO
 * @author wangshu
 * @date 2020/09/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectClusterLogicAuthPO extends BasePO  {
    /**
     * 主键
     */
    private Long id;

    /**
     * project id
     */
    private Integer projectId;

    /**
     * 逻辑集群ID
     */
    private Long logicClusterId;

    /**
     * 权限类型
     * @see ProjectClusterLogicAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表，id列表，英文逗号分隔
     */
    private String  responsible;
}