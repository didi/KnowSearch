package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;

import java.util.List;

/**
 * APP逻辑集群权限管理服务
 * @author wangshu
 * @date 2020/09/17
 */
public interface AppClusterLogicAuthService {

    /**
     * 设置APP对某逻辑集群的权限.
     * 封装了新增、更新、删除操作，调用接口时只需描述期望的权限状态
     * @param appId          APP的ID
     * @param logicClusterId 逻辑集群ID
     * @param auth           要设置的权限
     * @param responsible    责任人，逗号分隔的用户名列表
     * @return 设置结果
     */
    Result<Void> ensureSetLogicClusterAuth(Integer appId, Long logicClusterId, AppClusterLogicAuthEnum auth,
                                     String responsible, String operator);

    /**
     * 新增APP逻辑集群权限
     * @param logicClusterAuth APP逻辑集群权限
     * @param operator 操作者
     * @return
     */
    Result<Void> addLogicClusterAuth(AppLogicClusterAuthDTO logicClusterAuth, String operator);

    /**
     * 新增APP逻辑集群权限
     * @param logicClusterAuth APP逻辑集群权限
     * @param operator 操作者
     * @return
     */
    Result<Void> updateLogicClusterAuth(AppLogicClusterAuthDTO logicClusterAuth, String operator);

    /**
     * 删除APP逻辑集群权限
     * @param authId 权限点ID
     * @param operator 操作者
     * @return
     */
    Result<Void> deleteLogicClusterAuthById(Long authId, String operator);

    Result<Boolean> deleteLogicClusterAuthByLogicClusterId(Long logicClusterId);

    /**
     * 根据权限记录ID获取逻辑集群权限点
     * @param authId 权限点ID
     * @return
     */
    AppClusterLogicAuth getLogicClusterAuthById(Long authId);

    /**
     * 获取指定app对指定逻辑集群的权限.
     * @param appId          APP ID
     * @param logicClusterId 逻辑集群ID
     */
    AppClusterLogicAuthEnum getLogicClusterAuthEnum(Integer appId, Long logicClusterId);

    /**
     * 获取指定app对指定逻辑集群的权限，若没有权限则返回null.
     * 有权限时，返回结果中id不为null则为来自于权限表的数据，否则为来自于创建表的数据
     * @param appId          APP ID
     * @param logicClusterId 逻辑集群ID
     */
    AppClusterLogicAuth getLogicClusterAuth(Integer appId, Long logicClusterId);

    /**
     * 获取指定APP所有权限点
     * @param appId APP的ID
     * @return
     */
    List<AppClusterLogicAuth> getAllLogicClusterAuths(Integer appId);

    /**
     * 访问权限
     * @param appId
     * @return
     */
    List<AppClusterLogicAuth> getLogicClusterAccessAuths(Integer appId);

    /**
     * 获取指定逻辑集群指定类型的权限点
     * @param logicClusterId 逻辑集群ID
     * @param clusterAuthType 逻辑集群权限类型，为null则不筛选权限类型，返回改逻辑集群的全部权限点
     * @return
     */
    List<AppClusterLogicAuth> getLogicClusterAuths(Long logicClusterId, AppClusterLogicAuthEnum clusterAuthType);

    /**
     * 判断APP是否有在指定逻辑集群下创建索引的权限
     * @param appId APP的ID
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    boolean canCreateLogicTemplate(Integer appId, Long logicClusterId);

    /**
     * 增加权限  不做参数校验
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    Result<Void> addLogicClusterAuthWithoutCheck(AppLogicClusterAuthDTO authDTO, String operator);

    /**
     * 构建项目对物理集群的权限信息
     * @param appId                   项目
     * @param clusterLogicId          逻辑集群Id
     * @param appClusterLogicAuthEnum 权限点
     * @return
     */
    AppClusterLogicAuth buildClusterLogicAuth(Integer appId, Long clusterLogicId,
                                              AppClusterLogicAuthEnum appClusterLogicAuthEnum);

    /**
     * 获取全量权限信息
     * @return
     */
    List<AppClusterLogicAuth> list();
}
