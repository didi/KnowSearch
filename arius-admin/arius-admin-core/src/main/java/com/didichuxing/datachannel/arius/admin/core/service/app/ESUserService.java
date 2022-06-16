package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
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
     * @param esUser
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
     * 新建APP
     *
     * @param esUserDTO dto
     * @param operator  操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    Tuple</*创建的es user*/Result,/*创建的es user po*/ ESUserPO> registerESUser(ESUserDTO esUserDTO,
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
     * @param esUser esuser
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
    
    /**
     * 通过project 获取默认的es user
     *
     * @param projectId 项目id
     * @return {@code ESUser}
     */
    ESUser getDefaultESUserByProject(Integer projectId);
    
    boolean checkDefaultESUserByProject(Integer projectId);
    
    /**
     * 获取项目id通过搜索类型
     *
     * @param searchType 搜索类型
     * @return {@code List<Integer>}
     */
    List<Integer> getProjectIdBySearchType(Integer searchType);
    
    
}