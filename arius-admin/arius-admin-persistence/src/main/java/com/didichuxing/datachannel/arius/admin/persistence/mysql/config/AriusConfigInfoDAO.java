package com.didichuxing.datachannel.arius.admin.persistence.mysql.config;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.config.AriusConfigInfoPO;

/**
 * 配置表需要在开启mybatis中的二级缓存
 * @author d06679
 * @date 2019/3/14
 */
@Repository
public interface AriusConfigInfoDAO {

    int insert(AriusConfigInfoPO param);

    int update(AriusConfigInfoPO param);

    int updateByIdAndStatus(@Param("id") Integer id,
                            @Param("status") int status);

    int updateByGroupAndName(AriusConfigInfoPO param);

    List<AriusConfigInfoPO> listByCondition(AriusConfigInfoPO condt);

    AriusConfigInfoPO getbyId(Integer configId);

    AriusConfigInfoPO getByGroupAndName(@Param("valueGroup") String valueGroup,
                                        @Param("valueName") String valueName);

    List<AriusConfigInfoPO> listByGroup(String group);
}
