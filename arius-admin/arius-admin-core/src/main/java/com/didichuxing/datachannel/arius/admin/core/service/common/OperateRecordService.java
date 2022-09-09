package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
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
     * 手动触发保存操作记录
     *
     * @param content         操作记录的内容
     * @param operator        操作员是触发操作的用户。
     * @param projectId       项目编号
     * @param bizId           业务id，即业务表的id
     * @param operateTypeEnum 操作的类型，为枚举类，枚举类如下：
     */
    void saveOperateRecordWithManualTrigger(String content, String operator, Integer projectId, Object bizId,
                                            OperateTypeEnum operateTypeEnum) ;

    /**
     * 动态分页查询
     *
     * @param pageDTO 页面dto
     * @return {@code Object}
     */
    Tuple<Long, List<OperateRecordVO>> pagingGetOperateRecordByCondition(OperateRecordDTO pageDTO);

    OperateRecordVO getById(Integer id);
}