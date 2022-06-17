package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.oplog.OplogQueryDTO;
import com.didiglobal.logi.security.common.vo.oplog.OplogVO;

public interface OperateRecordManager {
    /**
     * oplogvo
     *
     * @param queryDTO 查询dto
     * @return {@code PagingResult<OplogVO>}
     */
    PagingResult<OplogVO> pageOplogPage(OplogQueryDTO queryDTO);
    
    /**
     * 获取oplog
     *
     * @param id id
     * @return {@code Result<OplogVO>}
     */
    Result<OplogVO> getOplogDetailByOplogId(Integer id);
}