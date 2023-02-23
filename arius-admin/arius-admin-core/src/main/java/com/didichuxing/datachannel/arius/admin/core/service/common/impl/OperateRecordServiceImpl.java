package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

import java.util.*;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.OperateRecordSortEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusTaskThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord.OperateRecordDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.knowframework.security.dao.ProjectDao;

/**
 *
 * @author d06679
 * @date 2019/3/14
 */
@Service
public class OperateRecordServiceImpl implements OperateRecordService {

    private static final ILog LOGGER          = LogFactory.getLog(OperateRecordServiceImpl.class);

    private static final String  PROJECT_NAME = "project_name";
    private static final String  MODULE_ID = "module_id";
    private static final String  OPERATE_ID = "operate_id";
    private static final String  TRIGGER_WAY_ID = "trigger_way_id";
    private static final String  USER_OPERATION = "user_operation";
    private static final String  ID = "id";

    private AriusTaskThreadPool ariusTaskThreadPool;

    @Autowired
    private OperateRecordDAO  operateRecordDAO;
    @Autowired
    private ProjectDao projectDao;

    @PostConstruct
    public void init() {
        ariusTaskThreadPool = new AriusTaskThreadPool();
        ariusTaskThreadPool.init(3, "OperateRecordServiceImpl", 100);
    }
    
    @Override
    public void deleteExprieData(Date saveTime) {

        operateRecordDAO.deleteExprieData(saveTime);
    
    }

    @Override
    public Result<Void> save(OperateRecord operateRecord) {
        ariusTaskThreadPool.run(() -> {
            try {
                OperateRecordInfoPO operateRecordInfoPO = ConvertUtil.obj2Obj(operateRecord, OperateRecordInfoPO.class);
                operateRecordDAO.insert(operateRecordInfoPO);
            } catch (Exception e) {
                LOGGER.error(
                        "class=OperateRecordServiceImply||method=save||errMsg={}||operateId={}",
                        e.getMessage(), operateRecord.getOperateId(), e);
            }
        });
        return Result.buildSucc();

    }

    @Override
    public void saveOperateRecordWithManualTrigger(String content, String operator, Integer projectId,
                                                   Object bizId, OperateTypeEnum operateTypeEnum,
                                                   Integer operateProjectId) {
        save(new OperateRecord.Builder().project(ConvertUtil.obj2Obj(projectDao.selectByProjectId(projectId),
                        ProjectBriefVO.class)).operationTypeEnum(operateTypeEnum)
                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).userOperation(operator).content(content).bizId(bizId)
                .operateProject(ConvertUtil.obj2Obj(projectDao.selectByProjectId(operateProjectId),ProjectBriefVO.class))
                .build());
    }

    @Override
    public void saveOperateRecordWithManualTrigger(String content, String operator, Integer projectId,
                                                   Object bizId, OperateTypeEnum operateTypeEnum) {
        save(new OperateRecord.Builder().project(ConvertUtil.obj2Obj(projectDao.selectByProjectId(projectId),
                        ProjectBriefVO.class)).operationTypeEnum(operateTypeEnum)
                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).userOperation(operator).content(content).bizId(bizId)
                .build());
    }

    @Override
    public void saveOperateRecordWithSchedulingTasks(String content, String operator, Integer projectId,
                                                                      Object bizId, OperateTypeEnum operateTypeEnum,
                                                                      Integer operateProjectId) {
        save(new OperateRecord.Builder().project(ConvertUtil.obj2Obj(projectDao.selectByProjectId(projectId),
                        ProjectBriefVO.class)).operationTypeEnum(operateTypeEnum)
                .triggerWayEnum(TriggerWayEnum.SCHEDULING_TASKS).userOperation(operator).content(content).bizId(bizId)
                .operateProject(ConvertUtil.obj2Obj(projectDao.selectByProjectId(operateProjectId),ProjectBriefVO.class))
                .build());
    }

    @Override
    public void saveOperateRecordWithSchedulingTasks(String content, String operator, Integer projectId, Object bizId,
                                                     OperateTypeEnum operateTypeEnum) {
       save(new OperateRecord.Builder().project(ConvertUtil.obj2Obj(projectDao.selectByProjectId(projectId),
                        ProjectBriefVO.class)).operationTypeEnum(operateTypeEnum)
                .triggerWayEnum(TriggerWayEnum.SCHEDULING_TASKS).userOperation(operator).content(content).bizId(bizId)
                .build());
    }
    
    /**
     * 动态分页查询
     *
     * @param pageDTO 页面dto
     * @return {@code Object}
     */
    @Override
    public Tuple<Long, List<OperateRecordVO>> pagingGetOperateRecordByCondition(OperateRecordDTO pageDTO) {
        String sortTerm = OperateRecordSortEnum.getSortField(pageDTO.getSortTerm());
        String sortType = pageDTO.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        pageDTO.setSortTerm(sortTerm);
        pageDTO.setSortType(sortType);
        if(StringUtils.isNotBlank(pageDTO.getProjectName())){
            pageDTO.setProjectName(CommonUtils.sqlFuzzyQueryTransfer(pageDTO.getProjectName()));
        }
        if(StringUtils.isNotBlank(pageDTO.getOperateProjectName())){
            pageDTO.setOperateProjectName(CommonUtils.sqlFuzzyQueryTransfer(pageDTO.getOperateProjectName()));
        }
        if(StringUtils.isNotBlank(pageDTO.getContent())){
            pageDTO.setContent(CommonUtils.sqlFuzzyQueryTransfer(pageDTO.getContent()));
        }
        final List<OperateRecordInfoPO> recordInfoPOList = operateRecordDAO.listByCondition(pageDTO);

        final Map</*id*/Integer, OperateRecordInfoPO> operateRecordInfoMap = ConvertUtil.list2Map(recordInfoPOList,
            OperateRecordInfoPO::getId);
        final List<OperateRecordVO> operateRecordVOList = ConvertUtil.list2List(recordInfoPOList,
            OperateRecordVO.class);
        //对vo中的数据进行转换
        Consumer<OperateRecordVO> poIncludeEnumIdConvertEnumStrFunc = operateRecordVO -> this
            .poIncludeEnumIdConvertEnumStr(operateRecordInfoMap.get(operateRecordVO.getId()), operateRecordVO);
        operateRecordVOList.forEach(poIncludeEnumIdConvertEnumStrFunc);

        final Long count = operateRecordDAO.countByCondition(pageDTO);

        return new Tuple<>(count, operateRecordVOList);
    }

    /**
     * po包括枚举id转换枚举str
     *
     * @param recordInfo 操作记录信息pomap
     * @param operateRecordVO        操作记录签证官
     */
    private void poIncludeEnumIdConvertEnumStr(OperateRecordInfoPO recordInfo, OperateRecordVO operateRecordVO) {
        //设置操作的模块
        Optional.ofNullable(recordInfo).map(OperateRecordInfoPO::getModuleId).map(ModuleEnum::getModuleEnum)
            .map(ModuleEnum::getModule).ifPresent(operateRecordVO::setModule);
        //设置触发方式
        Optional.ofNullable(recordInfo).map(OperateRecordInfoPO::getTriggerWayId).map(TriggerWayEnum::getTriggerWayEnum)
            .map(TriggerWayEnum::getTriggerWay).ifPresent(operateRecordVO::setTriggerWay);
        //设置操作类型
        Optional.ofNullable(recordInfo).map(OperateRecordInfoPO::getOperateId)
            .map(OperateTypeEnum::getOperationTypeEnum).map(OperateTypeEnum::getOperationType)
            .ifPresent(operateRecordVO::setOperate);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public OperateRecordVO getById(Integer id) {
        final OperateRecordInfoPO recordInfo = operateRecordDAO.getById(id);
        final OperateRecordVO operateRecordVO = ConvertUtil.obj2Obj(recordInfo, OperateRecordVO.class);
        poIncludeEnumIdConvertEnumStr(recordInfo, operateRecordVO);
        return operateRecordVO;
    }

    /**
     * 查询操作记录list
     * @param queryDTO
     * @return
     */
    @Override
    public List<OperateRecordVO> queryCondition(OperateRecordDTO queryDTO) {
        Result<Void> result = paramCheck(queryDTO.getUserOperation(), queryDTO.getProjectName());
        if (result.failed()) {
            return new ArrayList<>();
        }
        List<OperateRecordInfoPO> recordInfoPOList = operateRecordDAO.listByCondition(queryDTO);
        List<OperateRecordVO> operateRecordVOList = ConvertUtil.list2List(recordInfoPOList,
                OperateRecordVO.class);
        return operateRecordVOList;
    }

    @Override
    public Result<Integer> updateOperateRecord(OperateRecordDTO operateRecordDTO) {
        Result<Void> result = paramCheck(operateRecordDTO.getUserOperation(), operateRecordDTO.getProjectName());
        if (result.failed()) {
            return Result.buildFrom(result);
        }
        OperateRecordInfoPO selectOneOperateRecordInfoPO = operateRecordDAO.selectOneOperateRecord(operateRecordDTO);
        if (null == selectOneOperateRecordInfoPO) {
            return insertOperateRecordInfoWithCheck(operateRecordDTO);
        }
        OperateRecordInfoPO convertOperateRecordInfoPO = ConvertUtil.obj2Obj(operateRecordDTO, OperateRecordInfoPO.class);
        convertOperateRecordInfoPO.setUpdateTime(new Date());
        convertOperateRecordInfoPO.setOperateTime(new Date());
        convertOperateRecordInfoPO.setId(selectOneOperateRecordInfoPO.getId());
        boolean succ = (1 == operateRecordDAO.updateById(convertOperateRecordInfoPO));
        return Result.build(succ, convertOperateRecordInfoPO.getId());
    }

    /**
     * 新增操作记录
     * @param operateRecordDTO
     */
    private Result<Integer> insertOperateRecordInfoWithCheck(OperateRecordDTO operateRecordDTO) {
        OperateRecordInfoPO operateRecordInfoPO = ConvertUtil.obj2Obj(operateRecordDTO, OperateRecordInfoPO.class);
        operateRecordInfoPO.setOperateTime(new Date());
        boolean succ = (1 == operateRecordDAO.insertWithCheck(operateRecordInfoPO));
        return Result.build(succ, operateRecordInfoPO.getId());
    }

    /**
     * 校验应用和用户
     * @param userOperation
     * @param projectName
     * @return
     */
    private Result<Void> paramCheck(String userOperation, String projectName) {
        if (AriusObjUtils.isNull(userOperation)) {
            return Result.buildFail("用户账号为空");
        }
        if (AriusObjUtils.isNull(projectName)) {
            return Result.buildFail("应用为空");
        }
        return Result.buildSucc();
    }

}