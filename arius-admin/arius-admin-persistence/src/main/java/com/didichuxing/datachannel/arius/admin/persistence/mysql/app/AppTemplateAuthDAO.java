package com.didichuxing.datachannel.arius.admin.persistence.mysql.app;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppTemplateAuthPO;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Repository
public interface AppTemplateAuthDAO {

    List<AppTemplateAuthPO> listByCondition(AppTemplateAuthPO param);

    int insert(AppTemplateAuthPO param);

    int update(AppTemplateAuthPO param);

    int delete(Long authId);

    List<AppTemplateAuthPO> listWithRwAuthsByAppId(int appId);

    AppTemplateAuthPO getById(Long authId);

    AppTemplateAuthPO getByAppIdAndTemplateId(@Param("appId") Integer appId,
                                              @Param("templateId") String templateId);

    AppTemplateAuthPO getByAppIdAndTemplateIdAndType(@Param("appId") Integer appId,
                                                     @Param("templateId") String templateId,
                                                     @Param("type") Integer type);

    List<AppTemplateAuthPO> listByLogicTemplateId(String logicTemplateId);

    List<AppTemplateAuthPO> listWithRwAuths();

    List<AppTemplateAuthPO> listWithOwnerAuths();

}
