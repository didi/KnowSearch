package com.didiglobal.logi.op.manager.infrastructure.db.converter;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.db.ScriptPO;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-04 7:27 下午
 */
public class ScriptConverter {

    public static Script convertScriptPO2DO(ScriptPO scriptPO) {
        return ConvertUtil.obj2Obj(scriptPO, Script.class);
    }

    public static ScriptPO convertScriptDO2PO(Script script) {
        return ConvertUtil.obj2Obj(script, ScriptPO.class);
    }

    public static List<Script> convertScriptPO2DOList(List<ScriptPO> scriptPOList) {
        return ConvertUtil.list2List(scriptPOList, Script.class);
    }
}
