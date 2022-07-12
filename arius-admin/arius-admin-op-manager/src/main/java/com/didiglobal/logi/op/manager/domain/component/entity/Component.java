package com.didiglobal.logi.op.manager.domain.component.entity;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.ComponentStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 10:27 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Component {
    /**
     * 组件id
     */
    private String id;
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
    private String packageId;
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
    private Integer isDelete;

    /**
     * 值对象，关联group配置
     */
    private List<ComponentGroupConfig> groupConfigList;


    /**
     * 值对象，关联host
     */
    private List<ComponentHost> hostList;

    public Result<Void> checkInstallParam(){
        if (name.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "name缺失");
        }

        if (groupConfigList.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "配置组缺失");
        }

        if (null == packageId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "安装包缺失");
        }
        return Result.success();
    }

    public Component create(){
        this.status = ComponentStatusEnum.UN_KNOW.getStatus();
        this.isDelete = DeleteEnum.NORMAL.getType();
        this.groupConfigList.forEach(config -> {
            config.create();
        });
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

}
