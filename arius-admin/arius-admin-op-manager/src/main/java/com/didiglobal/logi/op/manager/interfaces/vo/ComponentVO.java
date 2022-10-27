package com.didiglobal.logi.op.manager.interfaces.vo;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentVO {
    /**
     * 组件id
     */
    private Integer id;
    /**
     * 状态(0 green,1 yellow,2 red,3 unKnow)
     */
    private Integer status;
    /**
     * 包含的组件id列表
     */
    private String containComponentIds;
    /**
     * 组件名
     */
    private String name;
    /**
     * 关联的安装包id
     */
    private Integer packageId;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 是否卸载
     */
    private Integer isDeleted;


    /**
     * 值对象，关联group配置
     */
    private List<ComponentGroupConfigVO> groupConfigList;

    /**
     * 关联外部任务
     */
    private String associationId;

    /**
     * 依赖的组件id
     */
    private Integer dependComponentId;

    /**
     * 值对象，关联host
     */
    private List<ComponentHostVO> hostList;

    /**
     * 依赖配置组件id
     */
    private Integer dependConfigComponentId;

}
