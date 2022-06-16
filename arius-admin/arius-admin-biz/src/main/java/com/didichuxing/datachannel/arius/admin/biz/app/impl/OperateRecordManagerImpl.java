package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.OperateRecordManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.oplog.OplogQueryDTO;
import com.didiglobal.logi.security.common.vo.oplog.OplogVO;
import org.springframework.stereotype.Component;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/16
 */
@Component
public class OperateRecordManagerImpl implements OperateRecordManager {
    /**
     * @param queryDTO
     * @return
     */
    @Override
    public PagingResult<OplogVO> getOplogPage(OplogQueryDTO queryDTO) {
        return null;
    }
    
    /**
     * @param id
     * @return
     */
    @Override
    public Result<OplogVO> getOplogDetailByOplogId(Integer id) {
        return null;
    }
}