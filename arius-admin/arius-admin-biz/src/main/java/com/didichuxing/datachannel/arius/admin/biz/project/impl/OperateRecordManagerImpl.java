package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import com.didichuxing.datachannel.arius.admin.biz.project.OperateRecordManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.impl.ClusterPhyManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.page.OperateRecordPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/17
 */
@Component
public class OperateRecordManagerImpl implements OperateRecordManager {
    private static final ILog    LOGGER = LogFactory.getLog(ClusterPhyManagerImpl.class);
    @Autowired
    private HandleFactory        handleFactory;
    @Autowired
    private OperateRecordService operateRecordService;

    /**
     * oplogvo
     *
     * @param queryDTO  查询dto
     * @param projectId
     * @return {@code PagingResult<OplogVO>}
     */
    @Override
    public PaginationResult<OperateRecordVO> pageOplogPage(OperateRecordDTO queryDTO,
                                                           Integer projectId) throws NotFindSubclassException {
        final BaseHandle baseHandle = handleFactory
            .getByHandlerNamePer(PageSearchHandleTypeEnum.OPERATE_RECORD.getPageSearchType());
        if (baseHandle instanceof OperateRecordPageSearchHandle) {
            OperateRecordPageSearchHandle pageSearchHandle = (OperateRecordPageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(queryDTO, projectId);
        }

        LOGGER.warn(
            "class=OperateRecordManagerImpl||method=pageOplogPage||msg=failed to get the OperateRecordPageSearchHandle");

        return PaginationResult.buildFail("操作日志获取失败");
    }

    /**
     * 获取oplog
     *
     * @param id id
     * @return {@code Result<OplogVO>}
     */
    @Override
    public Result<OperateRecordVO> getOplogDetailByOplogId(Integer id) {

        return Result.buildSucc(operateRecordService.getById(id));
    }
}