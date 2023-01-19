package com.didiglobal.logi.op.manager.domain.component.entity.value;

import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    /**
     * 机器规格
     */
    private String    machineSpec;
    public ComponentHost create() {
        return this.create(HostStatusEnum.ON_LINE.getStatus());
    }

    public ComponentHost create(int status) {
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        this.isDeleted = DeleteEnum.NORMAL.getType();
        this.status = status;
        return this;
    }
}