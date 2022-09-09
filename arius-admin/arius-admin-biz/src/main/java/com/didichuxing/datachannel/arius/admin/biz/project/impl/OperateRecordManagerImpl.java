package com.didichuxing.datachannel.arius.admin.biz.project.impl;

import com.didichuxing.datachannel.arius.admin.biz.page.OperateRecordPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.project.OperateRecordManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.Calendar;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/17
 */
@Component
public class OperateRecordManagerImpl implements OperateRecordManager {
    private static final ILog    LOGGER = LogFactory.getLog(OperateRecordManagerImpl.class);
    @Autowired
    private HandleFactory        handleFactory;
    @Autowired
    private OperateRecordService   operateRecordService;
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;
    
    /**
     * 0 0 1 * * ? 每天凌晨 1 点执行该方法 定时删除操作日志，根据配置中指定的保存天数对操作日志进行保留
     */
    @Scheduled(cron = "0 0 1 * * ?")
    private void scheduledDeletionOldOperateRecord() {
        Date saveTime = getSaveTime();
        operateRecordService.deleteExprieData(saveTime);
    }

    /**
     * 获得配置中设置的保存时间
     *
     * @param
     * @return Date
     */
    private Date getSaveTime() {
        Date currentTime = new Date();
        Date saveTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        calendar.add(Calendar.DAY_OF_MONTH,-ariusConfigInfoService.intSetting(
                AriusConfigConstant.ARIUS_COMMON_GROUP,AriusConfigConstant.OPERATE_RECORD_SAVE_TIME,
                AriusConfigConstant.OPERATE_RECORD_SAVE_TIME_DEFAULT_VALUE));
        saveTime = calendar.getTime();
        return saveTime;
    }

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