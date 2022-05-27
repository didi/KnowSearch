package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUserConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import java.util.List;

/**
 *es 用户操作
 *
 * @author shizeying
 * @date 2022/05/25
 */
public interface ESUserService {
    
    /**
     * 指定id查询
     *
     * @param esUser appID
     * @return app  如果不存在返回null
     */
    ESUser getEsUserById(Integer esUser);
    
    /**
     * 查询app详细信息
     *
     * @return 返回app列表
     */
    List<ESUser> listESUsers(List<Integer> projectIds);
    
    /**
     * 查询app详细信息
     *
     * @return List<App>
     */
    List<ESUser> listESUserWithCache(List<Integer> projectIds);
    
    /**
     * 新建APP
     *
     * @param esUserDTO dto
     * @param operator  操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    Tuple</*创建的es user*/Result<Integer>,/*创建的es user po*/ ESUserPO> registerESUser(ESUserDTO esUserDTO,
                                                                                   String operator);
    
    /**编辑用户
     * 编辑APP
     *
     * @param esUserDTO dto
     * @return Tuple<Result < Void>, ESUserPO>
     */
    Tuple<Result<Void>/*更新的状态*/, ESUserPO/*更新之后的的ESUserPO*/> editUser(ESUserDTO esUserDTO);
    
    /**
     * 删除APP
     *
     * @param esUser APPID
     * @return 成功 true  失败 false
     */
    Tuple<Result<Void>, ESUserPO> deleteESUserById(int esUser);
    
    Tuple<Result<Void>, List<ESUserPO>> deleteByESUsers(int projectId);
    
    /**
     * 获取项目下es user 个数
     *
     * @param projectId 项目id
     * @return int
     */
    int countByProjectId(int projectId);
    
    /**
     * 初始化es user配置
     * @param esUserName es user name
     * @return 成功 true  失败false
     *
     */
    Result<Void> initConfig(Integer esUserName);
    
    /**
     * 获取app id配置信息
     *
     * @param esUserName APP ID
     * @return 配置信息
     */
    ESUserConfig getESUserConfig(int esUserName);
    
    /**
     * listConfig
     *
     * @return List<App>
     */
    List<ESUserConfig> listConfig();
    
    /**
     * listConfigWithCache
     *
     * @return List<App>
     */
    List<ESUserConfig> listConfigWithCache();
    
    /**
     * 修改APP配置
     *
     * @param configDTO 配置信息
     * @param operator  操作人
     * @return 成功 true  失败  false
     */
    Tuple<Result<Void>, ESUserPO> updateESUserConfig(ESUserConfigDTO configDTO, String operator);
    
    /**
     * 校验app id是否存在
     * @param esUserName 应用id
     * @return true/false
     */
    boolean isESUserExists(Integer esUserName);
    
    /**
     * 判断app是否存在
     *
     * @param esUser app
     * @return true or false
     */
    boolean isESUserExists(ESUser esUser);
    
    /**
     * 根据appId判断是否为超级app
     * @param esUserName  esUserName
     * @return true or false
     */
    boolean isSuperESUser(Integer esUserName);
    
    /**
     * 校验验证码
     * @param esUserName app
     * @param verifyCode 验证码
     * @return result
     */
    Result<Void> verifyAppCode(Integer esUserName, String verifyCode);
    
    /**
     * 验证APP参数是否合法
     *
     * @param appDTO    dto
     * @param operation 是否校验null参数;  新建的时候需要校验,编辑的时候不需要校验
     * @return 参数合法返回
     */
    Result<Void> validateESUser(ESUserDTO appDTO, OperationEnum operation);
    
    /**
     * 查询项目下可以免密登录的es user
     * @param projectId projectId
     * @return appList
     */
    List<ESUser> getProjectWithoutCodeApps(Integer projectId);
    
}