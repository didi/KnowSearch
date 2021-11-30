package com.didichuxing.datachannel.arius.admin.common.bean.po.task.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcmTaskDetailPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键自增
     */
    private Long id;

    /**
     *  工单任务ID
     */
    private Long workOrderTaskId;

    /**
     *  所属角色
     */
    private String role;

    /**
     * 节点名称/主机名成
     */
    private String hostname;

    /**
     * 分组
     */
    private Integer grp;
    /**
     * 顺序
     */
    private Integer idx;

    /**
     * 状态
     */
    private String status;

    /**
     * 容器云 /物理机 接口返回任务ID
     */
    private Long taskId;
}
