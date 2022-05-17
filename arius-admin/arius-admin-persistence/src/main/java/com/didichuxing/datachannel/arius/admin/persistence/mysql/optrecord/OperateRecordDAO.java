package com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordInfoPO;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Repository
public interface OperateRecordDAO {
    /**
     * 通过条件查询列表
     * @param param OperateRecordInfoPO
     * @return List<OperateRecordPO>
     */
    List<OperateRecordInfoPO> listByCondition(OperateRecordInfoPO param);

    /**
     * 插入一条数据
     * @param po    OperateRecordInfoPO
     * @return      int
     */
    int insert(OperateRecordInfoPO po);

    /**
     * 当biz_id为空，将business_id设为默认值
     * @return  int
     */
    int compatible();

    /**
     * 通过moduleId和topN获取数据
     * @param moduleId 操作id
     * @param topN     top数
     * @return OperateRecordPO
     */
    OperateRecordInfoPO selectDescTopNByModuleId(@Param("moduleId") int moduleId, @Param("topN") int topN);

    /**
     * 通过moduleId和id删除数据
     * @param moduleId  操作id
     * @param id        id
     */
    void deleteByModuleIdAndLessThanId(@Param("moduleId")int moduleId, @Param("id") int id);
}
