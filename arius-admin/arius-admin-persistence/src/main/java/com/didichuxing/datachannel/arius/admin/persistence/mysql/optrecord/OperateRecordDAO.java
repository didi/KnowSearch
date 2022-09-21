package com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordInfoPO;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Repository
public interface OperateRecordDAO extends BaseMapper<OperateRecordInfoPO> {
    /**
     * 通过条件查询列表
     * @param param OperateRecordInfoPO
     * @return List<OperateRecordPO>
     */
    List<OperateRecordInfoPO> listByCondition(OperateRecordDTO param);

    /**
     * 总数计算
     *
     * @param param 入参
     * @return int
     */
    Long countByCondition(OperateRecordDTO param);

    /**
     * 插入一条数据
     * @param po    OperateRecordInfoPO
     * @return      int
     */
    @Override
    int insert(OperateRecordInfoPO po);

    /**
     * 通过moduleId和topN获取数据
     * @param moduleId 操作id
     * @param topN     top数
     * @return OperateRecordPO
     */
    OperateRecordInfoPO selectDescTopNByModuleId(@Param("moduleId") int moduleId, @Param("topN") int topN);

    /**
     * 删除saveTime之前的数据
     * @param saveTime 规定的保存时间
     * @return OperateRecordPO
     */
     void deleteExprieData(@Param("saveTime") Date saveTime);

    /**
     * 通过moduleId和id删除数据
     * @param moduleId  操作id
     * @param id        id
     */
    void deleteByModuleIdAndLessThanId(@Param("moduleId") int moduleId, @Param("id") int id);

    /**
     * 获取通过id
     *
     * @param id id
     * @return {@code OperateRecordInfoPO}
     */
    OperateRecordInfoPO getById(Integer id);
}