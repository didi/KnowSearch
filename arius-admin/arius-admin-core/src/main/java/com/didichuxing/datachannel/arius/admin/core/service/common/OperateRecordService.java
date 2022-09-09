package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import java.util.Date;
import java.util.List;

/**
 * 操作记录
 *
 * @author gyp
 * @version 1.0
 * @date 2022/5/10
 */
public interface OperateRecordService {
    /**
     * 删除早于指定时间的数据
     *
     * @param saveTime 删除数据的时间。
     */
   void deleteExprieData( Date saveTime);
    Result<Void> save(OperateRecord operateRecord);

    /**
     * 动态分页查询
     *
     * @param pageDTO 页面dto
     * @return {@code Object}
     */
    Tuple<Long, List<OperateRecordVO>> pagingGetOperateRecordByCondition(OperateRecordDTO pageDTO);

    OperateRecordVO getById(Integer id);
}