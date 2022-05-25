package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUserConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * es user 操作
 *
 * @author shizeying
 * @date 2022/05/25
 */
public interface ESUserManager {
    
    /**
     * 获取所有项目下全部的es user
     *
     * @return 返回app列表
     */
    Result<List<ESUser>> listESUsersByAllProject();
    
    /**
     * 通过项目 id 获取全部的es user
     *
     * @param projectId 项目id
     * @param operator
     * @return {@code List<ESUser>}
     */
    Result<List<ESUser>> listESUsersByProjectId(Integer projectId, String operator);
    
    /**
     * 验证APP参数是否合法
     * @param esUserDTO dto
     * @param operation 是否校验null参数;  新建的时候需要校验,编辑的时候不需要校验
     * @return 参数合法返回
     */
    Result<Void> validateESUser(ESUserDTO esUserDTO, OperationEnum operation);
    
    /**
     * 查询app详细信息
     *
     * @return 返回app构成的map，key为projectId, value为esusers
     */
    Result<Map</*projectId*/Integer, List<ESUser>>> getESUsersMap();
    
    /**
     * 新建APP
     *
     * @param appDTO    dto
     * @param projectId
     * @param operator  操作人
     * @return 成功 true  失败 false
     */
    Result<Integer> registerESUser(ESUserDTO appDTO, Integer projectId, String operator);
    
    /**
     * 指定es user查询应用的名称
     *
     * @param esUser appID
     * @return app的名称，不存在则返回null
     */
    Result</*projectName*/String> getProjectName(Integer esUser);
    
    /**
     * 更新 es user config
     *
     * @param configDTO configdto
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    Result<Void> updateESUserConfig(ESUserConfigDTO configDTO, String operator);
    
    /**
     * 编辑应用程序
     *
     * @param esUserDTO 应用dto
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    Result<Void> editESUser(ESUserDTO esUserDTO, String operator);
    
    /**
     * 删除项目下指定的es user
     * @param esUser esUser
     * @param operator 操作人
     * @return 成功 true  失败 false
     */
    Result<Void> deleteESUserByProject(int esUser, int projectId, String operator);
    
    /**
     * 删除项目下所有的es user
     *
     * @param projectId 项目id
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    Result<Void> deleteAllESUserByProject(int projectId, String operator);
    
    /**
     * 获取esUserName配置信息
     * @param esUserName esUserName
     * @return 配置信息
     */
    ESUserConfig getESUserConfig(int esUserName);
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
     * @return       true or false
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
     * 编辑APP接口
     * @param request    request
     * @param consoleESUserDTO     consoleESUserDTO
     * @return           Result<Void>
     */
    Result<Void> update(HttpServletRequest request, ConsoleESUserDTO consoleESUserDTO);
    
    Result<ConsoleESUserVO> get(Integer esUser);
    
    Result<List<ConsoleESUserVO>> list();
    
}