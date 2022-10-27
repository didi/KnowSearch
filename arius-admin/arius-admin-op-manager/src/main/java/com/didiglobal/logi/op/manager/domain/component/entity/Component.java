package com.didiglobal.logi.op.manager.domain.component.entity;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.ComponentStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.DeleteEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostStatusEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TSLEnum;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.Strings;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didiglobal.logi.op.manager.infrastructure.common.Constants.MAP_SIZE;

/**
 * @author didi
 * @date 2022-07-12 10:27 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Component {
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
    private List<ComponentGroupConfig> groupConfigList;

    /**
     * 依赖的组件id
     */
    private Integer dependComponentId;

    /**
     * 值对象，关联host
     */
    private List<ComponentHost> hostList;

    /**
     * 依赖配置组件id
     */
    private Integer dependConfigComponentId;

    /**
     * 用户名密码
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否开启tsl认证（0未开启，1开启）
     */
    private Integer isOpenTSL;


    public Component create() {
        this.status = ComponentStatusEnum.GREEN.getStatus();
        this.isDeleted = DeleteEnum.NORMAL.getType();
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        if (this.isOpenTSL == null) {
            this.isOpenTSL = TSLEnum.CLOSE.getType();
        }
        return this;
    }

    public Component updateContainIds(int componentId) {
        if (Strings.isNullOrEmpty(containComponentIds)) {
            containComponentIds = String.valueOf(componentId);
        } else {
            containComponentIds = containComponentIds + Constants.SPLIT + componentId;
        }
        return this;
    }

    public Component newDeployComponent() {
        this.setId(this.getDependConfigComponentId());
        for (ComponentGroupConfig componentGroupConfig : this.getGroupConfigList()) {
            componentGroupConfig.setComponentId(this.getDependConfigComponentId());
        }
        return this;
    }

    public Map<String, List<String>> groupNameToHost() {
        Map<String, List<String>> groupToHostList = new HashMap<>(MAP_SIZE);
        for (ComponentHost componentHost : this.getHostList()) {
            List<String> hostList = groupToHostList.computeIfAbsent(componentHost.getGroupName(), k -> new ArrayList<>());
            hostList.add(componentHost.getHost());
        }
        return groupToHostList;
    }

    /**
     * 获取子节点任务状态
     * @return 状态，全在线green，在线数小于离线数red，在线数大于离线数yellow
     */
    public int convergeHostStatus() {
        int onLineNum = 0;
        int offLineNum = 0 ;
        for (ComponentHost componentHost : this.getHostList()) {
            if (componentHost.getStatus() == HostStatusEnum.ON_LINE.getStatus()) {
                onLineNum ++;
            } else {
                offLineNum ++;
            }
        }
        if (offLineNum == 0) {
            return ComponentStatusEnum.GREEN.getStatus();
        } else if (onLineNum < offLineNum) {
            return ComponentStatusEnum.RED.getStatus();
        } else {
            return ComponentStatusEnum.YELLOW.getStatus();
        }
    }

    public void setGroupConfigList(List<ComponentGroupConfig> groupConfigList) {
        this.groupConfigList = ConvertUtil.list2List(groupConfigList, ComponentGroupConfig.class);
    }


}
