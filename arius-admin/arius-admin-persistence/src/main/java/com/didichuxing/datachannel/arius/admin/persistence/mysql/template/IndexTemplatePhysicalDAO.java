package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public interface IndexTemplatePhysicalDAO {

    List<TemplatePhysicalPO> listByCondition(TemplatePhysicalPO param);

    int insert(TemplatePhysicalPO param);

    TemplatePhysicalPO getById(Long physicalId);

    List<TemplatePhysicalPO> listByLogicId(Integer logicId);

    int update(TemplatePhysicalPO param);

    int updateStatus(@Param("physicalId") Long physicalId,
                     @Param("status") Integer status);

    List<TemplatePhysicalPO> getByClusterAndStatus(@Param("cluster") String cluster,
                                                   @Param("status") Integer status);

    List<TemplatePhysicalPO> getByClusterAndNameAndStatus(@Param("cluster") String cluster,
                                                               @Param("name") String name,
                                                               @Param("status") Integer status);

    List<TemplatePhysicalPO> listByClusterAndStatus(@Param("cluster") String cluster,
                                                    @Param("status") Integer status);

    List<TemplatePhysicalPO> listByMatchClusterAndStatus(@Param("cluster") String cluster,
                                                    @Param("status") Integer status);

    TemplatePhysicalPO getByClusterAndName(@Param("cluster") String cluster,
                                           @Param("name") String name);

    int deleteDirtyByClusterAndName(@Param("cluster") String cluster,
                                    @Param("name") String name);

    List<TemplatePhysicalPO> listByLogicIds(List<Integer> logicIds);

    List<TemplatePhysicalPO> listByIds(List<Long> physicalIds);

    List<TemplatePhysicalPO> listAll();

    List<TemplatePhysicalPO> listByName(String template);

    TemplatePhysicalPO getNormalAndDeletingById(Long physicalId);
}
