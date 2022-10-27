package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-19 5:35 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentHostPO {
    /**
     * 主机
     */
    private String host;
    /**
     * 组件id
     */
    private Integer componentId;
    /**
     * 状态，0在线，1离线
     */
    private Integer status;
    /**
     * 分组名
     */
    private String groupName;
    /**
     * 进程数量
     */
    private Integer processNum;
    /**
     * 是否卸载
     */
    private Integer isDeleted;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

}
