package com.didiglobal.logi.op.manager.domain.component.service.handler;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;

import java.util.Set;

/**
 * @author didi
 */
public interface ScaleHandler {

    /**
     * 扩缩容后对组件host进行相应操作（增加host信息）
     *
     * @param componentId
     * @param host
     * @param config
     */
    void dealComponentHost(int componentId, String host, ComponentGroupConfig config);

    /**
     * 扩缩容后对组件配置进行相应操作（增加）
     *
     * @param oriGroupConfig
     * @param newGroupConfig
     * @param hosts
     */
    void dealComponentGroupConfig(ComponentGroupConfig oriGroupConfig, ComponentGroupConfig newGroupConfig, Set<String> hosts);

    /**
     * 校验扩容是否能执行
     * @param scaleComponent
     * @return
     */
    Result<Void> check(GeneralScaleComponent scaleComponent);
}
