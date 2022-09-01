package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.*;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;

/**
 * @author didi
 * @date 2022-07-12 2:29 下午
 */
public class ComponentAssembler {

    public static Component toDO(ComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, Component.class);
    }

    public static GeneralInstallComponent toInstallComponent(GeneraInstallComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralInstallComponent.class);
    }

    public static Component toScaleComponent(GeneralScaleComponent scaleComponent) {
        Component component = ConvertUtil.obj2Obj(scaleComponent, Component.class);
        component.setId(scaleComponent.getComponentId());
        return component;
    }

    public static GeneralScaleComponent toScaleComponent(GeneralScaleComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralScaleComponent.class);
    }

    public static GeneralConfigChangeComponent toConfigChangeComponent(GeneralConfigChangeComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralConfigChangeComponent.class);
    }

    public static Component toConfigChangeComponent(GeneralConfigChangeComponent configChangeComponent) {
        Component component = ConvertUtil.obj2Obj(configChangeComponent, Component.class);
        component.setId(configChangeComponent.getComponentId());
        return component;
    }

    public static GeneralRestartComponent toRestartComponent(GeneralRestartComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralRestartComponent.class);
    }

    public static GeneralUpgradeComponent toUpgradeComponent(GeneralUpgradeComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralUpgradeComponent.class);
    }

    public static GeneralRollbackComponent toRollbackComponent(GeneralRollbackComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralRollbackComponent.class);
    }

    public static GeneralExecuteComponentFunction toExecuteFunctionComponent(GeneralExecuteComponentFunctionDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralExecuteComponentFunction.class);
    }

    public static GeneralGroupConfigHostVO toGeneralGroupConfigVO(GeneralGroupConfig config, String host) {
        GeneralGroupConfigHostVO vo = ConvertUtil.obj2Obj(config, GeneralGroupConfigHostVO.class);
        //设置目录
        vo.setInstallDirector(JSON.parseObject(config.getInstallDirectoryConfig()).get(host).toString());
        //设置进程数
        vo.setProcessNum(JSON.parseObject(config.getProcessNumConfig()).get(host).toString());
        return vo;
    }

}
