package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * APP逻辑模板权限控制服务
 * @author d06679
 * @date 2019/3/13
 */
public interface AppLogicTemplateAuthService {

    /**
     * 元数据校验
     * @param delete 是否执行删除操作
     * @return
     */
    boolean deleteExcessTemplateAuthsIfNeed(boolean delete);

    /**
     * 在逻辑模板权限表中设置APP对某逻辑模板的权限
     * 封装了新增、更新、删除操作，调用接口时只需描述期望的权限状态
     * @param appId           APP的ID
     * @param logicTemplateId 逻辑模板ID
     * @param auth            要设置的权限
     * @param responsible     责任人，逗号分隔的用户名列表
     * @return 设置结果
     */
    Result ensureSetLogicTemplateAuth(Integer appId, Integer logicTemplateId, AppTemplateAuthEnum auth,
                                      String responsible, String operator);

    /**
     * 获取APP有权限的逻辑模板权限点
     * @param appId APP ID
     * @return 模板权限
     */
    List<AppTemplateAuth> getTemplateAuthsByAppId(Integer appId);

    /**
     * 获取指定逻辑模板的模板权限点列表
     * @param logicTemplateId 逻辑模板id
     * @return 模板权限 WR R
     */
    List<AppTemplateAuth> getTemplateAuthsByLogicTemplateId(Integer logicTemplateId);

    /**
     * 获取指定逻辑模板的模板权限点列表
     */
    AppTemplateAuth getTemplateAuthByLogicTemplateIdAndAppId(Integer logicTemplateId, Integer appId);

    /**
     * 增加逻辑模板权限
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    Result addTemplateAuth(AppTemplateAuthDTO authDTO, String operator);

    /**
     * 修改逻辑模板权限，仅可以修改权限类型和责任人
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    Result updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator);

    /**
     * 删除逻辑模板权限
     * @param authId   APP ID
     * @param operator 操作人
     * @return result
     */
    Result deleteTemplateAuth(Long authId, String operator);

    /**
     * 获取所有APP的权限
     * @return map, key为APP ID, value为权限点信息
     */
    Map<Integer, Collection<AppTemplateAuth>> getAllAppTemplateAuths();

    /**
     * 获取当前appId对逻辑索引的权限
     * appId为超级项目, 有所有资源的管理权限
     */
    AppTemplateAuthEnum getAuthEnumByAppIdAndLogicId(Integer appId, Integer logicId);
}
