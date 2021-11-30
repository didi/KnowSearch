package com.didichuxing.datachannel.arius.admin.core.service.app;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

public interface AppService {

    /**
     * 指定id查询
     * @param appId appID
     * @return app  如果不存在返回null
     */
    App getAppById(Integer appId);

    /**
     * 指定id查询APP的名称
     * @param appId appID
     * @return app的名称，不存在则返回null
     */
    String getAppName(Integer appId);

    /**
     * 查询app详细信息
     * @return 返回app列表
     */
    List<App> listApps();

    /**
     * 查询app详细信息
     * @return
     */
    List<App> listAppWithCache();

    /**
     * 查询app详细信息
     * @return 返回app构成的map，key为appId, value为app
     */
    Map<Integer, App> getAppsMap();

    /**
     * 新建APP
     * @param appDTO dto
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    Result<Integer> registerApp(AppDTO appDTO, String operator);

    /**
     * 验证APP参数是否合法
     * @param appDTO dto
     * @param operation 是否校验null参数;  新建的时候需要校验,编辑的时候不需要校验
     * @return  参数合法返回
     */
    Result<Void> validateApp(AppDTO appDTO, OperationEnum operation);

    /**
     * 编辑APP
     * @param appDTO dto
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    Result<Void> editApp(AppDTO appDTO, String operator);

    /**
     * 删除APP
     * @param appId APPID
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    Result<Void> deleteAppById(int appId, String operator);

    /**
     * 初始化APP配置
     * @param appId APPID
     * @return 成功 true  失败false
     *
     */
    Result<Void> initConfig(Integer appId);

    /**
     * 获取app id配置信息
     * @param appId APP ID
     * @return 配置信息
     */
    AppConfig getAppConfig(int appId);

    /**
     * listConfig
     * @return
     */
    List<AppConfig> listConfig();

    /**
     * listConfigWithCache
     * @return
     */
    List<AppConfig> listConfigWithCache();

    /**
     * 修改APP配置
     * @param configDTO 配置信息
     * @param operator 操作人
     * @return 成功 true  失败  false
     */
    Result<Void> updateAppConfig(AppConfigDTO configDTO, String operator);

    /**
     * 校验app id是否存在
     * @param appId 应用id
     * @return true/false
     */
    boolean isAppExists(Integer appId);

    /**
     * 是否为超级管理员
     */
    boolean isSuperApp(Integer appId);

    /**
     * 用户登录接口
     * @param appId appId
     * @param verifyCode 校验码
     * @param operator 登陆人
     * @return result
     */
    Result<Void> login(Integer appId, String verifyCode, String operator);

    /**
     * 校验验证码
     * @param appId app
     * @param verifyCode 验证码
     * @return result
     */
    Result<Void> verifyAppCode(Integer appId, String verifyCode);

    /**
     * 查询用户可以免密登陆的APP列表,包含校验码谨慎使用
     * @param user 用户名
     * @return appList 
     */
    List<App> getUserLoginWithoutCodeApps(String user);

    /**
     * 根据责任人查询
     * @param responsible 责任者
     * @return list
     */
    List<App> getAppsByResponsibleId(Long responsible);

    /**
     * 根据app名称查询
     * @param name APP名称
     * @return app
     */
    App getAppByName(String name);

    /**
     * 获取对指定逻辑群的权限大于等于指定值的APP
     * 逻辑集群权限是递增的，所以指定logicClusterAuth为ACCESS时，有OWN权限的应用也会作为结果返回
     * @param logicClusterId 逻辑集群id
     * @param lowestLogicClusterAuth 需要拥有的最低逻辑集群权限
     * @return 对logicClusterId指定的集群权限大于lowestLogicClusterAuth的APP
     */
    List<App> getAppsByLowestLogicClusterAuth(Long logicClusterId, AppClusterLogicAuthEnum lowestLogicClusterAuth);
}
