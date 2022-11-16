package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptPageVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.op.manager.application.ScriptService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScriptPageSearchHandle extends AbstractPageSearchHandle<ScriptQueryDTO, ScriptPageVO> {
    @Autowired
    private ScriptService scriptService;

    @Override
    protected Result<Boolean> checkCondition(ScriptQueryDTO condition, Integer projectId) {
        return Result.buildSucc();
    }

    @Override
    protected void initCondition(ScriptQueryDTO condition, Integer projectId) {
        //do nothing
    }

    @Override
    protected PaginationResult<ScriptPageVO> buildPageData(ScriptQueryDTO queryDTO, Integer projectId) {
        Script script = ConvertUtil.obj2Obj(queryDTO, Script.class);
        List<Script> scriptList = null;
        Long count = 0L;
        try {
            scriptList = scriptService.pagingByCondition(script, queryDTO.getPage(), queryDTO.getSize());
            count = scriptService.countByCondition(script);
        } catch (Exception e) {
            LOGGER.error("class=ScriptPageSearchHandle||method=buildPageData||err={}",
                    e.getMessage(), e);
        }
        return PaginationResult.buildSucc(ConvertUtil.list2List(scriptList, ScriptPageVO.class), count, queryDTO.getPage(), queryDTO.getSize());
    }
}
