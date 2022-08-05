package com.didiglobal.logi.op.manager.infrastructure.db.converter;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentHost;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentGroupConfigPO;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentHostPO;
import com.didiglobal.logi.op.manager.infrastructure.db.ComponentPO;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

/**
 * @author didi
 * @date 2022-07-19 3:07 下午
 */
public class ComponentConverter {

    public static ComponentPO convertComponentDO2PO(Component component) {
        return ConvertUtil.obj2Obj(component, ComponentPO.class);
    }

    public static Component convertComponentPO2DO(ComponentPO componentPO) {
        return ConvertUtil.obj2Obj(componentPO, Component.class);
    }

    public static ComponentGroupConfigPO convertComponentConfigDO2PO(ComponentGroupConfig groupConfig) {
        return ConvertUtil.obj2Obj(groupConfig, ComponentGroupConfigPO.class);
    }

    public static ComponentGroupConfig convertComponentConfigPO2DO(ComponentGroupConfigPO po) {
        return ConvertUtil.obj2Obj(po, ComponentGroupConfig.class);
    }

    public static ComponentHostPO convertComponentHostDO2PO(ComponentHost componentHost) {
        return ConvertUtil.obj2Obj(componentHost, ComponentHostPO.class);
    }
}
