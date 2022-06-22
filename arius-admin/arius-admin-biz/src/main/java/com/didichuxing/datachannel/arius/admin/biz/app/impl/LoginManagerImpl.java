package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.LoginManager;
import com.didichuxing.datachannel.arius.admin.common.exception.OperateForbiddenException;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.dto.account.AccountLoginDTO;
import com.didiglobal.logi.security.common.enums.ResultCode;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.LoginService;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 登录管理器
 *
 * @author shizeying
 * @date 2022/06/16
 */
@Component
public class LoginManagerImpl implements LoginManager {
    @Autowired
    private LoginService   loginService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RoleTool       roleTool;
    
    /**
     * 验证登录信息（验证前密码先用Base64解码再用RSA解密） 登录前会检查账户激活状态
     *
     * @param loginDTO 登陆信息
     * @param request  请求信息
     * @param response
     * @return token
     * @throws LogiSecurityException 登录错误
     */
    @Override
    public Result<UserBriefVO> verifyLogin(AccountLoginDTO loginDTO, HttpServletRequest request,
                                           HttpServletResponse response) {
        
        try {
            UserBriefVO userBriefVO = loginService.verifyLogin(loginDTO, request, response);
            return Result.success(userBriefVO);
        } catch (LogiSecurityException e) {
            return Result.fail(e);
        }
    }
    
    /**
     * 登出接口
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        return loginService.logout(request, response);
    }
    
    /**
     * @param request
     * @param response
     * @param requestMappingValue
     * @param whiteMappingValues
     * @return
     * @throws IOException
     */
    @Override
    public boolean interceptorCheck(HttpServletRequest request, HttpServletResponse response,
                                    String requestMappingValue, List<String> whiteMappingValues,
                                    List<String> skipMappingValues) throws IOException {
        final boolean interceptorCheck = loginService.interceptorCheck(request, response, requestMappingValue,
                whiteMappingValues);
        if (interceptorCheck) {
            
            //跳过检查项目id和用户的正确性和匹配度
            if (skipMappingValues.stream()
                    .noneMatch(whiteMappingValue -> request.getServletPath().startsWith(whiteMappingValue))) {
                final Integer projectId = HttpRequestUtil.getProjectId(request);
                final String operator = HttpRequestUtil.getOperator(request);
                final Integer operatorId = HttpRequestUtil.getOperatorId(request);
                //项目id没有带
                if (Objects.isNull(projectId)) {
                    
                    throw new OperateForbiddenException(
                            String.format("请携带项目信息,HTTP_HEADER_KEY:%s", HttpRequestUtil.PROJECT_ID));
                }
                if (Objects.isNull(operatorId)) {
                    throw new OperateForbiddenException(
                            String.format("请携带操作者id,HTTP_HEADER_KEY:%s", HttpRequestUtil.USER_ID));
                    
                }
                if (StringUtils.isBlank(operator)) {
                    throw new OperateForbiddenException(
                            String.format("请携带操作者,HTTP_HEADER_KEY:%s", HttpRequestUtil.USER));
                    
                }
                if (!projectService.checkProjectExist(projectId)) {
                    throw new LogiSecurityException(ResultCode.PROJECT_NOT_EXISTS);
                }
                //判断用户在非管理员角色下，操作用户是否是当前项目成员或者拥有者
                if (!roleTool.isAdmin(operatorId)) {
                    final ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
                    if (!(CommonUtils.isUserNameBelongProjectMember(operator, projectVO)
                          || CommonUtils.isUserNameBelongProjectResponsible(operator, projectVO))) {
                        throw new LogiSecurityException(ResultCode.NO_PERMISSION);
                    }
                    
                }
            }
        }
        
        return interceptorCheck;
    }
}