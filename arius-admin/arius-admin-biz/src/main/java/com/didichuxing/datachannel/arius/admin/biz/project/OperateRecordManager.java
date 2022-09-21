package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

public interface OperateRecordManager {
    /**
     * oplogvo
     *
     * @param queryDTO  查询dto
     * @param projectId
     * @return {@code PagingResult<OplogVO>}
     */
    PaginationResult<OperateRecordVO> pageOplogPage(OperateRecordDTO queryDTO,
                                                    Integer projectId) throws NotFindSubclassException;

    /**
     * 获取oplog
     *
     * @param id id
     * @return {@code Result<OplogVO>}
     */
    Result<OperateRecordVO> getOplogDetailByOplogId(Integer id);

    /**
     * 获取DSL kibana操作记录 默认前30条
     * @return
     * @param queryDTO
     */
    Result<List<OperateRecordVO>> listSenseOperateRecord(OperateRecordDTO queryDTO, String operator, Integer projectId);

    /**
     * 更新sense操作记录
     * @param operateRecordDTO
     * @param operator
     * @param projectId
     * @return
     */
    Result<Integer> updateSenseOperateRecord(OperateRecordDTO operateRecordDTO, String operator, Integer projectId);
}