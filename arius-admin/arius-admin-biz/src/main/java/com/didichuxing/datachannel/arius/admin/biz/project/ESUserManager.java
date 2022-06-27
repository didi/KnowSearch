package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserWithVerifyCodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ESUserVO;
import com.didiglobal.logi.security.common.po.ProjectPO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import java.util.List;

/**
 * es user 操作
 *
 * @author shizeying
 * @date 2022/05/25
 */
public interface ESUserManager {
    

    
    /**
     * 通过项目id获取es user 列表
     *
     * @param projectId {@link ProjectPO#getId()}
     * @param operator  操作者属于{@link ProjectVO#getUserList()}
     * @return {@code List<ESUser>}
     */
    Result<List<ESUserVO>> listESUsersByProjectId(Integer projectId, String operator);
    

    

    
    /**
     * 新建APP
     *
     * @param appDTO    dto
     * @param projectId {@link ProjectPO#getId()}
     * @param operator  操作人 {@link   com.didichuxing.datachannel.arius.admin.core.component.RoleTool#isAdmin}
     * @return 成功 true  失败 false
     */
    Result<Integer> registerESUser(ESUserDTO appDTO, Integer projectId, String operator);

    

    
    /**
     * 编辑es user
     *
     * @param esUserDTO
     * @param operator 操作人 {@link   com.didichuxing.datachannel.arius.admin.core.component.RoleTool#isAdmin}
     * @return {@code Result<Void>}
     */
    Result<Void> editESUser(ESUserDTO esUserDTO, String operator);
    
    /**
     * 删除项目下的指定es user
     *
     * @param esUser ES用户
     * @param projectId {@link ProjectPO#getId()}
     * @param operator 操作人 {@link   com.didichuxing.datachannel.arius.admin.core.component.RoleTool#isAdmin}
     * @return {@code Result<Void>}
     */
    Result<Void> deleteESUserByProject(int esUser, int projectId, String operator);
    
    /**
     * 删除项目下所有的es user
     *
     * @param projectId {@link ProjectPO#getId()}
     * @param operator 操作人 {@link   com.didichuxing.datachannel.arius.admin.core.component.RoleTool#isAdmin}
     * @return {@code Result<Void>}
     */
    Result<Void> deleteAllESUserByProject(int projectId, String operator);
    
    
    /**
     * 校验验证码
     * @param esUserName es user
     * @param verifyCode 验证码
     * @return result
     */
    Result<Void> verifyAppCode(Integer esUserName, String verifyCode);
    

    
    /**
     * 获取
     *
     * @param esUser ES用户
     * @return {@code Result<ConsoleESUserVO>}
     */
    Result<ConsoleESUserVO> get(Integer esUser);

    
    /**
     * 获取没有
     *
     * @param projectId
     * @param operator
     * @return {@code Result<List<ConsoleESUserWithVerifyCodeVO>>}
     */
    Result<List<ConsoleESUserWithVerifyCodeVO>> getNoCodeESUser(Integer projectId, String operator);
    
}