package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
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
     * 手动触发保存操作记录，包含用户操作的应用
     *
     * @param content         操作记录的内容
     * @param operator        操作员是触发操作的用户。
     * @param projectId       项目编号
     * @param bizId           业务id，即业务表的id
     * @param operateTypeEnum 操作的类型，为枚举类，枚举类如下：
     * @param operateProjectId 用户操作的应用，即资源所属应用
     */
    void saveOperateRecordWithManualTrigger(String content, String operator, Integer projectId, Object bizId,
                                            OperateTypeEnum operateTypeEnum, Integer operateProjectId) ;

    /**
     * 保存项目的运行记录
     *
     * @param content 操作记录的内容
     * @param operator 运营商名称
     * @param projectId 项目编号
     * @param bizId 操作记录的业务ID，即任务的ID。
     * @param operateTypeEnum 操作的类型，为枚举类型，枚举类型如下：
     */
    void saveOperateRecordWithSchedulingTasks(String content, String operator, Integer projectId, Object bizId,
                                            OperateTypeEnum operateTypeEnum) ;

    /**
     * 保存项目的运行记录
     *
     * @param content 操作记录的内容
     * @param operator 运营商名称
     * @param projectId 项目编号
     * @param bizId 操作记录的业务ID，即任务的ID。
     * @param operateTypeEnum 操作的类型，为枚举类型，枚举类型如下：
     * @param operateProjectId 资源所属应用
     */
    void saveOperateRecordWithSchedulingTasks(String content, String operator, Integer projectId, Object bizId,
                                              OperateTypeEnum operateTypeEnum, Integer operateProjectId) ;

    /**
     * 动态分页查询
     *
     * @param pageDTO 页面dto
     * @return {@code Object}
     */
    Tuple<Long, List<OperateRecordVO>> pagingGetOperateRecordByCondition(OperateRecordDTO pageDTO);

    OperateRecordVO getById(Integer id);

 /**
  * 获取操作记录list
  * @param queryDTO
  * @return
  */
 List<OperateRecordVO> queryCondition(OperateRecordDTO queryDTO);

    /**
     * 更新操作记录
     * @param operateRecordDTO
     * @return
     */
    Result<Integer> updateOperateRecord(OperateRecordDTO operateRecordDTO);
}