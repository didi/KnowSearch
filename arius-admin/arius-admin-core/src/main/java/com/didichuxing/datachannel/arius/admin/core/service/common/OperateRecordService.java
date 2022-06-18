package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import java.util.List;

/**
 * 操作记录
 *
 * @author gyp
 * @version 1.0
 * @date 2022/5/10
 */
public interface OperateRecordService {



    Result<Void> save(OperateRecord operateRecord);
    /**
     * 插入一条操作记录
     *
     * @param moduleEnum    操作记录模块枚举
     * @param operationEnum 操作枚举
     * @param bizId         业务id
     * @param content       内容
     * @param operator      操作对象
     * @return Result
     */
    Result<Void> save(ModuleEnum moduleEnum, OperationEnum operationEnum, Object bizId, String content, String operator);

    /**
     * 插入一条操作记录
     *
     * @param param OperateRecordDTO
     * @return 成功 true   失败 false
     */
    Result<Void> save(OperateRecordDTO param);




    
    /**
     * 动态分页查询
     *
     * @param pageDTO 页面dto
     * @return {@code Object}
     */
    Tuple<Long,List<OperateRecordVO>> pagingGetOperateRecordByCondition(OperateRecordDTO pageDTO);
    
    OperateRecordVO getById(Integer id);
}