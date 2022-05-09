package com.didichuxing.datachannel.arius.admin.core.service.common;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

public interface OperateRecordService {

    /**
     * 根据指定的查询条件查询
     * @param condt 查询条件dto
     * @return 操作记录列表
     */
    Result<List<OperateRecordVO>> list(OperateRecordDTO condt);

    /**
     * 插入一条操作记录
     * @param moduleId 模块id  比如索引模板、应用管理、DSL审核
     * @param operateId 操作行为  OperationEnum
     * @param bizId 业务id  例如索引模板id、应用id 或者工单id
     * @param content 操作详情
     * @param operator 操作人
     * @return 成功 true   失败 false
     *
     */
    Result<Void> save(int moduleId, int operateId, String bizId, String content, String operator);

    /**
     * 插入一条操作记录
     * @param moduleEnum
     * @param operationEnum
     * @param bizId
     * @param content
     * @param operator
     * @return
     */
    Result<Void> save(ModuleEnum moduleEnum, OperationEnum operationEnum, Object bizId, String content, String operator);

    /**
     * 插入一条操作记录
     * @return 成功 true   失败 false
     *
     */
    Result<Void> save(OperateRecordDTO param);

    /**
     * 查询某个最新的操作记录
     * @param moduleId 模块id
     * @param operateId 操作行为
     * @param bizId 业务id
     * @param beginDate 起始时间 时间范围是:[beginDate, beginDate + 24h]
     * @return 如果没有一条 返回 null
     */
    OperateRecord getLastRecord(int moduleId, int operateId, String bizId, Date beginDate);

    /**
     * 根据指定的查询条件批量查询
     *
     * @param bizId
     * @param moduleIds
     * @return 操作记录列表
     */
    Result<List<OperateRecordVO>> multiList(String bizId, List<Integer> moduleIds);

}
