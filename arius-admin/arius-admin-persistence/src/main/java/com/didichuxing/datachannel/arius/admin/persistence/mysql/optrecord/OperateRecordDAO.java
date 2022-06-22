package com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordInfoPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

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
    List<OperateRecordInfoPO> listByCondition(@Param("param") OperateRecordDTO param);
    
    /**
     * 总数计算
     *
     * @param param 入参
     * @return int
     */
    Long countByCondition(@Param("param") OperateRecordDTO param);

    /**
     * 插入一条数据
     * @param po    OperateRecordInfoPO
     * @return      int
     */
    int insert(OperateRecordInfoPO po);



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
    
    /**
     * 获取通过id
     *
     * @param id id
     * @return {@code OperateRecordInfoPO}
     */
    OperateRecordInfoPO getById(Integer id);
}