package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.OpTaskVO;
import com.didichuxing.datachannel.arius.admin.core.service.task.OpTaskService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TaskPageSearchHandle extends AbstractPageSearchHandle<OpTaskQueryDTO, OpTaskVO>{
    private static final ILog LOGGER = LogFactory.getLog(TaskPageSearchHandle.class);
    @Autowired
    private OpTaskService opTaskService;
    @Override
    protected Result<Boolean> checkCondition(OpTaskQueryDTO condition, Integer projectId) {
        return Result.buildSucc();
    }

    @Override
    protected void initCondition(OpTaskQueryDTO condition, Integer projectId) {
        //do nothing
    }

    @Override
    protected PaginationResult<OpTaskVO> buildPageData(OpTaskQueryDTO queryDTO, Integer projectId) {
        Tuple<Long, List<OpTaskVO>> tuple = opTaskService.pagingGetTasksByCondition(queryDTO);
        if (tuple == null) {
            return PaginationResult.buildSucc(Collections.emptyList(), 0L, queryDTO.getPage(), queryDTO.getSize());
        }
        return PaginationResult.buildSucc(tuple.getV2(), tuple.v1(), queryDTO.getPage(), queryDTO.getSize());
    }
}
