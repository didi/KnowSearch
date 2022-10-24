package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author chengxiang
 * @date 2022/5/9
 */
@Repository
public interface IndexTemplatePhyDAO {

    List<IndexTemplatePhyPO> listByCondition(IndexTemplatePhyPO param);

    int insert(IndexTemplatePhyPO param);

    IndexTemplatePhyPO getById(Long physicalId);

    List<IndexTemplatePhyPO> listByLogicId(Integer logicId);
    IndexTemplatePhyPO getTemplateByLogicIdAndRole(@Param("logicId") Integer logicId, @Param("role") Integer role);

    List<IndexTemplatePhyPO> getByLogicIdAndStatus(@Param("logicId") Integer logicId, @Param("status") Integer status);

    int update(IndexTemplatePhyPO param);

    int updateStatus(@Param("physicalId") Long physicalId, @Param("status") Integer status);

    List<IndexTemplatePhyPO> getByClusterAndStatus(@Param("cluster") String cluster, @Param("status") Integer status);

    List<IndexTemplatePhyPO> getByClusterAndNameAndStatus(@Param("cluster") String cluster, @Param("name") String name,
                                                          @Param("status") Integer status);

    List<IndexTemplatePhyPO> listByClusterAndStatus(@Param("cluster") String cluster, @Param("status") Integer status);

    List<IndexTemplatePhyPO> listByMatchClusterAndStatus(@Param("cluster") String cluster,
                                                         @Param("status") Integer status);

    IndexTemplatePhyPO getByClusterAndName(@Param("cluster") String cluster, @Param("name") String name);

    int deleteDirtyByClusterAndName(@Param("cluster") String cluster, @Param("name") String name);

    List<IndexTemplatePhyPO> listByLogicIds(List<Integer> logicIds);

    List<IndexTemplatePhyPO> listByIds(List<Long> physicalIds);

    List<IndexTemplatePhyPO> listAll();

    List<IndexTemplatePhyPO> listByName(String template);

    IndexTemplatePhyPO getNormalAndDeletingById(Long physicalId);

    /**
     * 用于聚合logicId计数，仅查询了logicId和id
     * @return
     */
    List<IndexTemplatePhyPO> countListByLogicId();

    List<IndexTemplatePhyPO> listByRegionId(Integer regionId);
    
    int updateShardNumByLogicId(@Param("logicId")Integer logicId, @Param("shardNum")Integer shardNum);
}