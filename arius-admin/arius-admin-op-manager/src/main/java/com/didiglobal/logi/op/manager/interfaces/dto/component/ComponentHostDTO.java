package com.didiglobal.logi.op.manager.interfaces.dto.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-12 2:26 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentHostDTO {
    /**
     * 主机名
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
     * 目录
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
