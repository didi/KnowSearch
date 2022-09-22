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
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import java.util.*;
import java.util.stream.Collectors;

import com.didiglobal.logi.security.service.ProjectService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;

/**
 * 操作记录
 *
 * @author shizeying
 * @date 2022/06/17
 */
@Component
public class OperateRecordManagerImpl implements OperateRecordManager {
    private static final ILog    LOGGER = LogFactory.getLog(OperateRecordManagerImpl.class);
    private static final Long    FROM = 0L;
    private static final Long    SIZE = 1L;
    @Autowired
    private HandleFactory        handleFactory;
    @Autowired
    private OperateRecordService   operateRecordService;
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;
    @Autowired
    private ProjectService projectService;
    
    /**
     * 0 0 1 * * ? 每天凌晨 1 点执行该方法 定时删除操作日志，根据配置中指定的保存天数对操作日志进行保留
     */
    @Scheduled(cron = "0 0 1 * * ?")
    private void scheduledDeletionOldOperateRecord() {
        Date saveTime = getSaveTime();
        LOGGER.info(
                "class=OperateRecordServiceImpl||method=scheduledDeletionOldOperateRecord||msg= 操作日志定时删除任务开始执行");
        try {
            operateRecordService.deleteExprieData(saveTime);
        } catch (Exception e) {
            LOGGER.error("class=OperateRecordServiceImpl||method=scheduledDeletionOldOperateRecord||errMsg={}",
                    e.getMessage());
        }
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

    /**
     * sense查询记录
     * @param queryDTO 查询条件
     * @param operator 用户名称
     * @param projectId 项目id
     * @return
     */
    @Override
    public Result<List<String>> listSenseOperateRecord(OperateRecordDTO queryDTO, String operator, Integer projectId) {
        //组装查询条件
        buildCommonOperateRecordDTO(projectId, queryDTO, operator);
        queryDTO.setFrom(FROM);
        queryDTO.setSize(SIZE);
        List<OperateRecordVO> operateRecordVOList = operateRecordService.queryCondition(queryDTO);
        List<String> operateRecordList = new ArrayList<>();
        if(CollectionUtils.isEmpty(operateRecordVOList)){
            //查询平台配置中的超级应用的默认命令
            List<String> superAppDefaultCommandList = new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP
                    , AriusConfigConstant.SUPER_APP_DEFALT_DSL_COMMAND,
                    AriusConfigConstant.SUPER_APP_DEFALT_DSL_COMMAND_VALUE, COMMA));
            operateRecordList.addAll(superAppDefaultCommandList);
        }else{
            operateRecordList = operateRecordVOList.stream().map(OperateRecordVO::getContent).collect(Collectors.toList());
        }
        return Result.buildSucc(operateRecordList);
    }

    @Override
    public Result<Integer> updateSenseOperateRecord(OperateRecordDTO operateRecordDTO, String operator, Integer projectId) {
        buildCommonOperateRecordDTO(projectId, operateRecordDTO, operator);
        Result<Integer> result = operateRecordService.updateOperateRecord(operateRecordDTO);
        if (result.failed()) {
            LOGGER.warn("class=OperateRecordManagerImpl||method=updateSenseOperateRecord||errMsg={}",
                    "用户DSL查询记录更新出错");
        }
        return result;
    }

    /**
     * 组装参数
     *
     * @param projectId
     * @param operateRecordDTO
     * @param operator
     */
    private void buildCommonOperateRecordDTO(Integer projectId, OperateRecordDTO operateRecordDTO, String operator) {
        String projectName = projectService.getProjectBriefByProjectId(projectId).getProjectName();
        operateRecordDTO.setOperateId(OperateTypeEnum.DSL_QUERY_RECORD.getCode());
        operateRecordDTO.setModuleId(ModuleEnum.DSL_QUERY.getCode());
        operateRecordDTO.setTriggerWayId(TriggerWayEnum.MANUAL_TRIGGER.getCode());
        operateRecordDTO.setUserOperation(operator);
        operateRecordDTO.setProjectName(projectName);
    }
}