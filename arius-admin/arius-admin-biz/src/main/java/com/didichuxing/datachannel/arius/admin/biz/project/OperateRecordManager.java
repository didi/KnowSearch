package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

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
}