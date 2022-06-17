package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.OperateRecordManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.oplog.OplogQueryDTO;
import com.didiglobal.logi.security.common.vo.oplog.OplogVO;
import org.springframework.stereotype.Component;

@Component
public class OperateRecordManagerImpl implements OperateRecordManager {
    /**
     * oplogvo
     *
     * @param queryDTO 查询dto
     * @return {@code PagingResult<OplogVO>}
     */
    @Override
    public PagingResult<OplogVO> pageOplogPage(OplogQueryDTO queryDTO) {
        return null;
    }
    
    /**
     * 获取oplog
     *
     * @param id id
     * @return {@code Result<OplogVO>}
     */
    @Override
    public Result<OplogVO> getOplogDetailByOplogId(Integer id) {
        return null;
    }
}