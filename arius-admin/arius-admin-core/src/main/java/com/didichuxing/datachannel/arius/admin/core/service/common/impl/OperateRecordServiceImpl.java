package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

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
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord.OperateRecordDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.dao.ProjectDao;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author d06679
 * @date 2019/3/14
 */
@Service
public class OperateRecordServiceImpl implements OperateRecordService {

    private static final ILog LOGGER          = LogFactory.getLog(OperateRecordServiceImpl.class);

    @Autowired
    private OperateRecordDAO  operateRecordDAO;
    @Autowired
    private ProjectDao projectDao;
   


    
    @Override
    public void deleteExprieData(Date saveTime) {
    
        operateRecordDAO.deleteExprieData(saveTime);
    
    }
    
    
    @Override
    public Result<Void> save(OperateRecord operateRecord) {
        final OperateRecordInfoPO operateRecordInfoPO = ConvertUtil.obj2Obj(operateRecord, OperateRecordInfoPO.class);
        return Result.build(operateRecordDAO.insert(operateRecordInfoPO) == 1);

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

}