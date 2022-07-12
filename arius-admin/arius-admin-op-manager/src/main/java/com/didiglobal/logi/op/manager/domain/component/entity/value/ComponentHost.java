package com.didiglobal.logi.op.manager.domain.component.entity.value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 10:36 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentHost {
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
     * 目录
     */
    private String directory;
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
