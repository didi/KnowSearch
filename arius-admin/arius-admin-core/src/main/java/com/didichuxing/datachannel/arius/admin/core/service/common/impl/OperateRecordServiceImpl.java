package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordPO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord.OperateRecordDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 *
 * @author d06679
 * @date 2019/3/14
 */
@Service
public class OperateRecordServiceImpl implements OperateRecordService {

    private static final ILog LOGGER = LogFactory.getLog(OperateRecordServiceImpl.class);
    private static final int     MAX_RECORD_COUNT = 200;

    @Autowired
    private OperateRecordDAO  operateRecordDAO;

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
        List<OperateRecordPO> deleteList = new ArrayList<>();
        for(OperationEnum operationEnum : operationEnums) {
            // 获取每一个分类倒数第 N 条数据
            int moduleId = operationEnum.getCode();
            OperateRecordPO operateRecordPO = operateRecordDAO.selectDescTopNByModuleId(moduleId, SAVE_RECENT_NUM);
            if(operateRecordPO == null) {
                // 说明这个分类数据一共不超过 N 条
                continue;
            }
            deleteList.add(operateRecordPO);
        }
        for(OperateRecordPO operateRecordPO : deleteList) {
            // 删除该类别中，比指定id小的数据
            operateRecordDAO.deleteByModuleIdAndLessThanId(operateRecordPO.getModuleId(), operateRecordPO.getId());
        }
    }

    /**
     * 根据指定的查询条件查询
     * @param condt 查询条件dto
     * @return 操作记录列表
     */
    @Override
    public Result<List<OperateRecordVO>> list(OperateRecordDTO condt) {
        if (condt == null) {
            return Result.buildSucc(Lists.newArrayList());
        }
        List<OperateRecordVO> records = ConvertUtil.list2List(operateRecordDAO.listByCondition(ConvertUtil.obj2Obj(condt, OperateRecordPO.class)), OperateRecordVO.class);
        if (records.size() > MAX_RECORD_COUNT) {
            records = records.subList(0, MAX_RECORD_COUNT);
        }
        fillVOField(records);
        return Result.buildSucc(records);
    }

    /**
     * 插入一条操作记录
     * @param moduleEnum    模块id  比如索引模板、应用管理、DSL审核
     * @param operationEnum 操作行为  OperationEnum
     * @param bizId     业务id  例如索引模板id、应用id 或者工单id
     * @param content   操作详情
     * @param operator  操作人
     * @return result
     */
    @Override
    public Result<Void> save(ModuleEnum moduleEnum, OperationEnum operationEnum, Object bizId, String content,
                       String operator) {
        return save(moduleEnum.getCode(), operationEnum.getCode(), String.valueOf(bizId), content, operator);
    }

    /**
     * 插入一条操作记录
     * @param moduleId  模块id  比如索引模板、应用管理、DSL审核
     * @param operateId 操作行为  OperationEnum
     * @param bizId     业务id  例如索引模板id、应用id 或者工单id
     * @param content   操作详情
     * @param operator  操作人
     * @return result
     */
    @Override
    public Result<Void> save(int moduleId, int operateId, String bizId, String content, String operator) {
        if (operator == null) {
            operator = AriusUser.UNKNOWN.getDesc();
        }

        OperateRecordDTO param = new OperateRecordDTO();
        param.setModuleId(moduleId);
        param.setOperateId(operateId);
        param.setBizId(bizId);
        param.setContent(content);
        param.setOperator(operator);

        return save(param);
    }

    @Override
    public Result<Void> save(OperateRecordDTO param) {
        Result<Void> checkResult = checkParam(param);

        if (checkResult.failed()) {
            LOGGER.warn("class=OperateRecordServiceImpl||method=save||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        if (OperationEnum.EDIT.getCode() == param.getOperateId()
            && AriusObjUtils.isNull(param.getContent())) {
            return Result.buildSucc();
        }

        return Result.build(operateRecordDAO.insert(ConvertUtil.obj2Obj(param, OperateRecordPO.class)) == 1);
    }

    /**
     * 查询某个最新的操作记录
     * @param moduleId  模块id
     * @param operateId 操作行为
     * @param bizId     业务id
     * @param beginDate 起始时间 时间范围是:[beginDate, beginDate + 24h]
     * @return 如果没有一条 返回 null
     */
    @Override
    public OperateRecord getLastRecord(int moduleId, int operateId, String bizId, Date beginDate) {
        OperateRecordPO condt = new OperateRecordPO();
        condt.setModuleId(moduleId);
        condt.setOperateId(operateId);
        condt.setBizId(bizId);
        condt.setBeginTime(AriusDateUtils.getZeroDate(beginDate));
        condt.setEndTime(AriusDateUtils.getAfterDays(condt.getBeginTime(), 1));

        List<OperateRecordPO> pos = operateRecordDAO.listByCondition(condt);

        if (CollectionUtils.isEmpty(pos)) {
            return null;
        }

        return ConvertUtil.obj2Obj(pos.get(0), OperateRecord.class);
    }

    /**
     * 根据指定的查询条件批量查询
     *
     * @param bizId
     * @param moduleIds
     * @return 操作记录列表
     */
    @Override
    public Result<List<OperateRecordVO>> multiList(String bizId, List<Integer> moduleIds) {
        if (bizId == null) {
            return Result.buildSucc(Lists.newArrayList());
        }
        List<OperateRecordVO> operateRecordVOS= new ArrayList<>();
        for (Integer moduleId : moduleIds) {
            OperateRecordDTO operateRecordDTO = new OperateRecordDTO();
            operateRecordDTO.setBizId(bizId);
            operateRecordDTO.setModuleId(moduleId);
            Result<List<OperateRecordVO>> result = list(operateRecordDTO);
            operateRecordVOS.addAll(result.getData());
        }
        operateRecordVOS.sort(Comparator.comparing(OperateRecordVO::getOperateTime).reversed());
        return Result.buildSucc(operateRecordVOS);
    }

    /******************************************* private method **************************************************/
    private Result<Void> checkParam(OperateRecordDTO param) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("记录为空");
        }
        if (AriusObjUtils.isNull(param.getModuleId())) {
            return Result.buildParamIllegal("模块为空");
        }
        if (AriusObjUtils.isNull(param.getOperateId())) {
            return Result.buildParamIllegal("操作为空");
        }
        if (AriusObjUtils.isBlack(param.getBizId())) {
            return Result.buildParamIllegal("业务id为空");
        }
        if (AriusObjUtils.isNullStr(param.getContent())) {
            return Result.buildParamIllegal("操作内容为空");
        }
        if (AriusObjUtils.isBlack(param.getOperator())) {
            return Result.buildParamIllegal("操作人为空");
        }
        if (!ModuleEnum.validate(param.getModuleId())) {
            return Result.buildParamIllegal("模块非法");
        }
        if (!OperationEnum.validate(param.getOperateId())) {
            return Result.buildParamIllegal("操作非法");
        }

        return Result.buildSucc();
    }

    private void fillVOField(List<OperateRecordVO> records) {
        if(CollectionUtils.isEmpty(records)){return;}

        for(OperateRecordVO vo : records){
            vo.setModule(ModuleEnum.valueOf(vo.getModuleId()).getDesc());
            vo.setOperate( OperationEnum.valueOf(vo.getOperateId()).getDesc());
        }
    }
}
