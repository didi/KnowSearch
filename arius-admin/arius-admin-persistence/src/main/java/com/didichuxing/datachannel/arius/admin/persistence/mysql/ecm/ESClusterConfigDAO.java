package com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.po.esconfig.ESConfigPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lyn
 * @date 2020-12-30
 */
@Repository
public interface ESClusterConfigDAO {
    List<ESConfigPO> listByClusterId(Long clusterId);

    ESConfigPO getByClusterIdAndTypeAndEngin(@Param("clusterId") Long clusterId,
                                             @Param("type") String type,
                                             @Param("engin") String engin);
    int insert(ESConfigPO param);

    int update(ESConfigPO param);

    ESConfigPO getValidEsConfigById(Long id);

    ESConfigPO getById(Long id);

    int delete(Long id);

    ESConfigPO getByClusterIdAndTypeAndEnginAndVersion(@Param("clusterId") Long clusterId,
                                                       @Param("type") String type,
                                                       @Param("engin") String enginName,
                                                       @Param("version") Integer version);

    int updateConfigValidById(Long id);

    int insertSelective(ESConfigPO param);

    int deleteByClusterIdAndTypeAndEngin(@Param("clusterId") Long clusterId,
                                         @Param("type") String type,
                                         @Param("engin") String enginName);
    
    Integer getClusterIdByConfigId(@Param("id")Long configId);
}