package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.dto.ScriptDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.ScriptVO;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-06 2:48 下午
 */
public class ScriptAssembler {
    public static Script toDO(ScriptDTO dto) {
        return ConvertUtil.obj2Obj(dto, Script.class);
    }

    public static ScriptDTO toDTO(Script script) {
        return ConvertUtil.obj2Obj(script, ScriptDTO.class);
    }

    public static List<ScriptVO> toVOList(List<Script> scriptList) {
        return ConvertUtil.list2List(scriptList, ScriptVO.class);
    }
}
