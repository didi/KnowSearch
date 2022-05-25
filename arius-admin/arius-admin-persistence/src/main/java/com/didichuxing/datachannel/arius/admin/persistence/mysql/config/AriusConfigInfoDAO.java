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

    /**
     * 插入平台配置
     * @param param po
     * @return 插入数量
     */
    int insert(AriusConfigInfoPO param);

    /**
     * 更新平台配置
     * @param param po
     * @return 更新数量
     */
    int update(AriusConfigInfoPO param);

    /**
     * 通过id去更新status
     * @param id 平台配置id
     * @param status 状态
     * @return 更新数量
     */
    int updateByIdAndStatus(@Param("id") Integer id,
                            @Param("status") int status);

    /**
     * 通过条件获取平台配置
     * @param condition 条件
     * @return 平台配置list
     */
    List<AriusConfigInfoPO> listByCondition(AriusConfigInfoPO condition);

    /**
     * 通过id获取平台配置
     * @param configId 配置id
     * @return 平台配置po
     */
    AriusConfigInfoPO getById(Integer configId);

    /**
     * 通过group和name获取平台配置
     * @param valueGroup 配置组
     * @param valueName 配置名称
     * @return 平台配置po
     */
    AriusConfigInfoPO getByGroupAndName(@Param("valueGroup") String valueGroup,
                                        @Param("valueName") String valueName);

    /**
     * 通过配置组获取平台配置list
     * @param group 配置组
     * @return 配置po
     */
    List<AriusConfigInfoPO> listByGroup(String group);
}
