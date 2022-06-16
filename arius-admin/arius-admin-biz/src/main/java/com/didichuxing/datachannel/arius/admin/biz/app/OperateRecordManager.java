package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.oplog.OplogQueryDTO;
import com.didiglobal.logi.security.common.vo.oplog.OplogVO;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/16
 */
public interface OperateRecordManager {
    PagingResult<OplogVO> getOplogPage(OplogQueryDTO queryDTO);
    
    Result<OplogVO> getOplogDetailByOplogId(Integer id);
}