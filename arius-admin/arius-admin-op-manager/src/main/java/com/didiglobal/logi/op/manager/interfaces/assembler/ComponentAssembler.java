package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.didiglobal.logi.op.manager.domain.component.entity.Component;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.ComponentDTO;

/**
 * @author didi
 * @date 2022-07-12 2:29 下午
 */
public class ComponentAssembler {

    public static Component toDO(ComponentDTO dto) {
        return ConvertUtil.obj2Obj(dto, Component.class);
    }
}
