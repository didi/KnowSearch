package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.ComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.GeneraInstallComponentDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.GeneralScaleComponentDTO;
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

    public static GeneralScaleComponent toScaleComponent(GeneralScaleComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, GeneralScaleComponent.class);
    }


    public static GeneralGroupConfigHostVO toGeneralGroupConfigVO(GeneralGroupConfig config, String host) {
        GeneralGroupConfigHostVO vo = ConvertUtil.obj2Obj(config, GeneralGroupConfigHostVO.class);
        StringBuilder directory = new StringBuilder();
        JSON.parseObject(config.getInstallDirectoryConfig()).forEach((name, dc) -> {
            if (name.equals(host)) {
                if (0 == directory.length()) {
                    directory.append(dc);
                } else {
                    directory.append(",").append(dc);
                }

            }
        });

        vo.setInstallDirector(directory.toString());
        return vo;
    }
}
