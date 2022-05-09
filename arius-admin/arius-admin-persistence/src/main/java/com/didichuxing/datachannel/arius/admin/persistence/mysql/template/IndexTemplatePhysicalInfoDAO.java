package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhysicalInfoPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author chengxiang
 * @date 2022/5/9
 */
@Repository
public interface IndexTemplatePhysicalInfoDAO {

    List<IndexTemplatePhysicalInfoPO> listByCondition(IndexTemplatePhysicalInfoPO param);

    int insert(IndexTemplatePhysicalInfoPO param);

    IndexTemplatePhysicalInfoPO getById(Long physicalId);

    List<IndexTemplatePhysicalInfoPO> listByLogicId(Integer logicId);

    int update(IndexTemplatePhysicalInfoPO param);

    int updateStatus(@Param("physicalId") Long physicalId,
                     @Param("status") Integer status);

    List<IndexTemplatePhysicalInfoPO> getByClusterAndStatus(@Param("cluster") String cluster,
                                                            @Param("status") Integer status);

    List<IndexTemplatePhysicalInfoPO> getByClusterAndNameAndStatus(@Param("cluster") String cluster,
                                                                   @Param("name") String name,
                                                                   @Param("status") Integer status);

    List<IndexTemplatePhysicalInfoPO> listByClusterAndStatus(@Param("cluster") String cluster,
                                                             @Param("status") Integer status);

    List<IndexTemplatePhysicalInfoPO> listByMatchClusterAndStatus(@Param("cluster") String cluster,
                                                                  @Param("status") Integer status);

    IndexTemplatePhysicalInfoPO getByClusterAndName(@Param("cluster") String cluster,
                                                    @Param("name") String name);

    int deleteDirtyByClusterAndName(@Param("cluster") String cluster,
                                    @Param("name") String name);

    List<IndexTemplatePhysicalInfoPO> listByLogicIds(List<Integer> logicIds);

    List<IndexTemplatePhysicalInfoPO> listByIds(List<Long> physicalIds);

    List<IndexTemplatePhysicalInfoPO> listAll();

    List<IndexTemplatePhysicalInfoPO> listByName(String template);

    IndexTemplatePhysicalInfoPO getNormalAndDeletingById(Long physicalId);

    /**
     * 用于聚合logicId计数，仅查询了logicId和id
     * @return
     */
    List<IndexTemplatePhysicalInfoPO> countListByLogicId();
}
