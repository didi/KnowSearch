package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUserConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserWithVerifyCodeVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didiglobal.logi.security.common.po.ProjectPO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.service.ProjectService;
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
     * {@link ProjectService#getProjectBriefList()} 获取全部项目
     * {@link ProjectPO#getId()} 去获取全量的es user
     * @return 返回es user 列表
     */
    Result<List<ESUser>> listESUsers();
    
    /**
     * 通过项目id获取es user 列表
     *
     * @param projectId {@link ProjectPO#getId()}
     * @param operator 操作者属于{@link ProjectVO#getUserList()}
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
     * @param projectId {@link ProjectPO#getId()}
     * @param operator  操作人 {@link AdminConstant#SUPER_USER_NAME}
     * @return 成功 true  失败 false
     */
    Result<Integer> registerESUser(ESUserDTO appDTO, Integer projectId, String operator);
    
    /**
     * 指定es user查询应用的名称
     *
     * @param esUser es user
     * @return {@link ProjectPO#getProjectName()}
     */
    Result</*projectName*/String> getProjectName(Integer esUser);
    
    /**
     * 更新 es user config
     *
     * @param configDTO configdto
     * @param operator 操作人 {@link AdminConstant#SUPER_USER_NAME}
     * @return {@code Result<Void>}
     */
    Result<Void> updateESUserConfig(ESUserConfigDTO configDTO, String operator);
    
    /**
     * 编辑es user
     *
     * @param esUserDTO
     * @param operator 操作人 {@link AdminConstant#SUPER_USER_NAME}
     * @return {@code Result<Void>}
     */
    Result<Void> editESUser(ESUserDTO esUserDTO, String operator);
    
    /**
     * 删除项目下的指定es user
     *
     * @param esUser ES用户
     * @param projectId {@link ProjectPO#getId()}
     * @param operator 操作人 {@link AdminConstant#SUPER_USER_NAME}
     * @return {@code Result<Void>}
     */
    Result<Void> deleteESUserByProject(int esUser, int projectId, String operator);
    
    /**
     * 删除项目下所有的es user
     *
     * @param projectId {@link ProjectPO#getId()}
     * @param operator 操作人 {@link AdminConstant#SUPER_USER_NAME}
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
     * 根据es user name 判断是否为root es user
     * @param esUserName  esUserName
     * @return       true or false
     */
    boolean isSuperESUser(Integer esUserName);

    
    /**
     * 校验验证码
     * @param esUserName es user
     * @param verifyCode 验证码
     * @return result
     */
    Result<Void> verifyAppCode(Integer esUserName, String verifyCode);
    
    /**
     * 编辑APP接口
     *
     * @param projectId
     * @param userName
     * @param consoleESUserDTO consoleESUserDTO
     * @return Result<Void>
     */
    Result<Void> update(Integer projectId,String userName, ConsoleESUserDTO consoleESUserDTO);
    
    /**
     * 获取
     *
     * @param esUser ES用户
     * @return {@code Result<ConsoleESUserVO>}
     */
    Result<ConsoleESUserVO> get(Integer esUser);
    
    /**
     * 列表
     *
     * @return {@code Result<List<ConsoleESUserVO>>}
     */
    Result<List<ConsoleESUserVO>> list();
    
    Result<List<ConsoleESUserWithVerifyCodeVO>> getNoCodeESUser(HttpServletRequest request);
}