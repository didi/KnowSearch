package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.NewModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord.OperateRecordDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author d06679
 * @date 2019/3/14
 */
@Service
public class OperateRecordServiceImpl implements OperateRecordService {

    private static final ILog LOGGER = LogFactory.getLog(OperateRecordServiceImpl.class);

    @Autowired
    private OperateRecordDAO operateRecordDAO;

    /**
     * 操作日志，每个类别，保留的最近操作日志数
     */
    private static final int SAVE_RECENT_NUM = 1000;

    /**
     * 0 0 1 * * ?
     * 每天凌晨1点执行该方法
     * 定时删除操作日志，保留不同分类指定数量的最近操作日志
     */
    @Scheduled(cron = "0 0 1 * * ?")
    private void scheduledDeletionOldOperateRecord() {
        LOGGER.info("class=OperateRecordServiceImpl||method=scheduledDeletionOldOperateRecord||msg=操作日志定时删除任务开始执行");
        // 获取所有的分类
        OperationEnum[] operationEnums = OperationEnum.values();
        List<OperateRecordInfoPO> deleteList = new ArrayList<>();
        for(OperationEnum operationEnum : operationEnums) {
            // 获取每一个分类倒数第 N 条数据
            int moduleId = operationEnum.getCode();
            OperateRecordInfoPO operateRecordPO = operateRecordDAO.selectDescTopNByModuleId(moduleId, SAVE_RECENT_NUM);
            if(operateRecordPO == null) {
                // 说明这个分类数据一共不超过 N 条
                continue;
            }
            deleteList.add(operateRecordPO);
        }
        for(OperateRecordInfoPO operateRecord : deleteList) {
            // 删除该类别中，比指定id小的数据
            operateRecordDAO.deleteByModuleIdAndLessThanId(operateRecord.getModuleId(), operateRecord.getId());
        }
    }




    /**
     * @param operateRecord
     * @return
     */
    @Override
    public Result<Void> save(OperateRecord operateRecord) {
        final OperateRecordInfoPO operateRecordInfoPO = ConvertUtil.obj2Obj(operateRecord, OperateRecordInfoPO.class);
        return Result.build(operateRecordDAO.insert(operateRecordInfoPO) == 1);
        
    }

    
    /**
     * 动态分页查询
     *
     * @param pageDTO 页面dto
     * @return {@code Object}
     */
    @Override
    public Tuple<Long, List<OperateRecordVO>> pagingGetOperateRecordByCondition(OperateRecordDTO pageDTO) {
        String sortTerm = null == pageDTO.getSortTerm() ? SortConstant.ID : pageDTO.getSortTerm();
        String sortType = pageDTO.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        pageDTO.setSortTerm(sortTerm);
        pageDTO.setSortType(sortType);
        final List<OperateRecordInfoPO> recordInfoPOList = operateRecordDAO.listByCondition(pageDTO);
        
        final Map</*id*/Integer, OperateRecordInfoPO> operateRecordInfoMap = ConvertUtil.list2Map(recordInfoPOList,
                OperateRecordInfoPO::getId);
        final List<OperateRecordVO> operateRecordVOList = ConvertUtil.list2List(recordInfoPOList, OperateRecordVO.class);
        //对vo中的数据进行转换
        Consumer<OperateRecordVO> poIncludeEnumIdConvertEnumStrFunc = operateRecordVO -> this.poIncludeEnumIdConvertEnumStr(
                operateRecordInfoMap.get(operateRecordVO.getId()), operateRecordVO);
        operateRecordVOList.forEach(poIncludeEnumIdConvertEnumStrFunc);
       
        final Long count = operateRecordDAO.countByCondition(pageDTO);
        
    
        return new Tuple<>(count,operateRecordVOList);
    }
    
    /**
     * po包括枚举id转换枚举str
     *
     * @param recordInfo 操作记录信息pomap
     * @param operateRecordVO        操作记录签证官
     */
    private void poIncludeEnumIdConvertEnumStr(OperateRecordInfoPO recordInfo, OperateRecordVO operateRecordVO) {
        //设置操作的模块
        Optional.ofNullable(recordInfo)
                .map(OperateRecordInfoPO::getModuleId).map(NewModuleEnum::getModuleEnum)
                .map(NewModuleEnum::getModule).ifPresent(operateRecordVO::setModule);
        //设置触发方式
        Optional.ofNullable(recordInfo)
                .map(OperateRecordInfoPO::getTriggerWayId).map(TriggerWayEnum::getTriggerWayEnum)
                .map(TriggerWayEnum::getTriggerWay).ifPresent(operateRecordVO::setTriggerWay);
        //设置操作类型
        Optional.ofNullable(recordInfo)
                .map(OperateRecordInfoPO::getOperateId).map(OperateTypeEnum::getOperationTypeEnum)
                .map(OperateTypeEnum::getOperationType).ifPresent(operateRecordVO::setOperate);
    }
    
    /**
     * @param id
     * @return
     */
    @Override
    public OperateRecordVO getById(Integer id) {
        final OperateRecordInfoPO recordInfo = operateRecordDAO.getById(id);
        final OperateRecordVO operateRecordVO = ConvertUtil.obj2Obj(recordInfo, OperateRecordVO.class);
        poIncludeEnumIdConvertEnumStr(recordInfo,operateRecordVO);
        return operateRecordVO;
    }
    


}