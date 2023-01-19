package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.*;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.component.ComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.general.*;
import com.didiglobal.logi.op.manager.interfaces.vo.ComponentVO;
import com.didiglobal.logi.op.manager.interfaces.vo.GeneralGroupConfigHostVO;
import com.didiglobal.logi.op.manager.interfaces.vo.ScriptVO;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-12 2:29 下午
 */
public class ComponentAssembler {

    public static Component toDO(ComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, Component.class);
    }

    public static List<ComponentVO> toVOList(List<Component> componentList) {
        return ConvertUtil.list2List(componentList, ComponentVO.class);
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

    public static Result<GeneralGroupConfigHostVO> toGeneralGroupConfigVO(GeneralGroupConfig config, String host) {
        GeneralGroupConfigHostVO vo = ConvertUtil.obj2Obj(config, GeneralGroupConfigHostVO.class);
        String processNum = JSON.parseObject(config.getProcessNumConfig()).getString(host);
        String directory = JSON.parseObject(config.getInstallDirectoryConfig()).getString(host);
        if (null == processNum || null == directory) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), String.format("未匹配到对应的host[%s]", host));
        }
        //设置目录
        vo.setInstallDirector(directory);
        //设置进程数
        vo.setProcessNum(processNum);
        return Result.success(vo);
    }

}
