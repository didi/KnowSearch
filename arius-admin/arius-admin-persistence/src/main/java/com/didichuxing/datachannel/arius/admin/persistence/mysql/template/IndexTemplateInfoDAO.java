package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplateInfoPO;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public interface IndexTemplateInfoDAO {

    List<IndexTemplateInfoPO> listByCondition(IndexTemplateInfoPO param);

    int insert(IndexTemplateInfoPO param);

    int update(IndexTemplateInfoPO param);

    int delete(Integer logicId);

    IndexTemplateInfoPO getById(Integer logicId);

    List<IndexTemplateInfoPO> listByAppId(Integer appId);

    List<IndexTemplateInfoPO> listAll();

    List<IndexTemplateInfoPO> listByIds(List<Integer> logicIds);

    List<IndexTemplateInfoPO> listByDataCenter(String dataCenter);

    List<IndexTemplateInfoPO> listByName(String name);

    List<IndexTemplateInfoPO> likeByCondition(IndexTemplateInfoPO param);

    List<IndexTemplateInfoPO> pagingByCondition(@Param("name") String name, @Param("dataType") Integer dataType,
                                                @Param("hasDCDR") Boolean hasDCDR,
                                                @Param("from") Long from, @Param("size") Long size,
                                                @Param("sortTerm") String sortTerm, @Param("sortType") String sortType);

    long getTotalHitByCondition(IndexTemplateInfoPO param);

    List<IndexTemplateInfoPO> likeByResponsible(String responsible);

    int batchChangeHotDay(Integer days);

    int updateBlockReadState(@Param("logicId") Integer logicId, @Param("blockRead") Boolean blockRead);

    int updateBlockWriteState(@Param("logicId") Integer logicId, @Param("blockWrite") Boolean blockWrite);

    List<String> listAllNames();
}
