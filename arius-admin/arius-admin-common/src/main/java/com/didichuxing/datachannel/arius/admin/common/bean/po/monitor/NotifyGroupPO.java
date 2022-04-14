package com.didichuxing.datachannel.arius.admin.common.bean.po.monitor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notify_group")
public class NotifyGroupPO extends BasePO {
    private Long id;
    /**
     * 夜莺的userGroupId
     */
    private Long userGroupId;
    /**
     * 项目id
     */
    private Long appId;
    /**
     * 告警组名称
     */
    private String name;
    /**
     * 告警组成员 id;name,id;name
     */
    private String members;
    /**
     * 告警组的备注信息
     */
    private String comment;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 状态. -1 删除, 0 停用, 1 启用.
     */
    private Integer status;
}
