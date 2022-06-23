package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import static com.didiglobal.logi.security.util.HttpRequestUtil.COOKIE_OR_SESSION_MAX_AGE_UNIT_SEC;

import com.didichuxing.datachannel.arius.admin.biz.app.LoginManager;
import com.didichuxing.datachannel.arius.admin.common.exception.OperateForbiddenException;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuple;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuple3;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
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
import com.didiglobal.logi.security.util.AESUtils;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.Cookie;
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
    private              LoginService   loginService;
    @Autowired
    private              ProjectService projectService;
    @Autowired
    private              RoleTool       roleTool;
    public static final  String         KNOW_SEARCH_TOKEN = "knowSearchToken";
    private static final String         STRING_SPLIT      = "||";
    
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
            //response设置平台独有cookie
            addCookieByKnowSearch(userBriefVO.getUserName(), response);
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
                                    String requestMappingValue, List<String> whiteMappingValues) throws IOException {
        boolean interceptorCheck = loginService.interceptorCheck(request, response, requestMappingValue,
                whiteMappingValues);
        Tuple3</*username*/String,/*userId*/Integer,/*projectId*/Integer> userNameAndUserIdAndProjectIdTuple3 = getRequestByHead(
                request);
        //当用户登录成功/或者跳过白名单之后，就默认用户已经存在了header cookie等信息，此时需要确认项目和用户的一致性
        if (interceptorCheck) {
        
            //跳过检查项目id和用户的正确性和匹配度
            if (whiteMappingValues.stream()
                    .noneMatch(whiteMappingValue -> request.getServletPath().startsWith(whiteMappingValue))) {
                if (hasLoginValidExtend(userNameAndUserIdAndProjectIdTuple3._1, request)) {
                
                    //项目id没有带
                    if (Objects.isNull(userNameAndUserIdAndProjectIdTuple3._3)) {
                    
                        throw new OperateForbiddenException(
                                String.format("请携带项目信息,HTTP_HEADER_KEY:%s", HttpRequestUtil.PROJECT_ID));
                    }
                    if (Objects.isNull(userNameAndUserIdAndProjectIdTuple3._2)) {
                        throw new OperateForbiddenException(
                                String.format("请携带操作者id,HTTP_HEADER_KEY:%s", HttpRequestUtil.USER_ID));
                    
                    }
                    if (StringUtils.isBlank(userNameAndUserIdAndProjectIdTuple3._1)) {
                        throw new OperateForbiddenException(
                                String.format("请携带操作者,HTTP_HEADER_KEY:%s", HttpRequestUtil.USER));
                    
                    }
                
                    if (!projectService.checkProjectExist(userNameAndUserIdAndProjectIdTuple3._3)) {
                        throw new LogiSecurityException(ResultCode.PROJECT_NOT_EXISTS);
                    }
                    //判断用户在非管理员角色下，操作用户是否是当前项目成员或者拥有者
                    if (!roleTool.isAdmin(userNameAndUserIdAndProjectIdTuple3._2)) {
                        final ProjectVO projectVO = projectService.getProjectDetailByProjectId(
                                userNameAndUserIdAndProjectIdTuple3._3);
                        if (!(CommonUtils.isUserNameBelongProjectMember(userNameAndUserIdAndProjectIdTuple3._1,
                                projectVO) || CommonUtils.isUserNameBelongProjectResponsible(
                                userNameAndUserIdAndProjectIdTuple3._1, projectVO))) {
                            throw new LogiSecurityException(ResultCode.NO_PERMISSION);
                        }
                    
                    }
                
                }
            }
        }
        
        return interceptorCheck;
    }
    
    private void addCookieByKnowSearch(String userName, HttpServletResponse response) {
        //这里对domainAccount 进行加密处理，避免用户通过自行修改domainAccount进行替换用户的场景
        String plaintext = userName + STRING_SPLIT + System.currentTimeMillis();
        String ciphertext = AESUtils.encrypt(plaintext);
        Cookie cookieCiphertext = new Cookie(KNOW_SEARCH_TOKEN, ciphertext);
        cookieCiphertext.setMaxAge(COOKIE_OR_SESSION_MAX_AGE_UNIT_SEC);
        cookieCiphertext.setPath("/");
        response.addCookie(cookieCiphertext);
    }
    
    /**
     * 扩展验证ks是否篡改登录信息
     *
     * @param userName 用户名
     * @param request  请求
     * @return boolean
     */
    private boolean hasLoginValidExtend(String userName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (AriusObjUtils.isNull(cookies)) {
            return false;
        }
        return Arrays.stream(cookies).filter(cookie -> KNOW_SEARCH_TOKEN.equals(cookie.getName()))
                .map(cookie -> AESUtils.decrypt(cookie.getValue())).filter(Objects::nonNull)
                .map(plaintext -> StringUtils.split(plaintext, STRING_SPLIT))
                .filter(plaintexts -> plaintexts.length == 2).map(plaintexts -> plaintexts[0])
                .anyMatch(domainAccountInner -> StringUtils.equals(domainAccountInner, userName));
    }
    
    /**
     * 通过请求头获取user name usernameId projectId
     *
     * @param request 请求
     * @return {@code Tuple3<String, Integer, Integer>}
     */
    private Tuple3</*username*/String,/*userId*/Integer,/*projectId*/Integer> getRequestByHead(
            HttpServletRequest request) {
        Tuple3<String, Integer, Integer> tuple3 = Tuple.of(null, null, null);
        final String operator = HttpRequestUtil.getOperator(request);
        final Integer operatorId = HttpRequestUtil.getOperatorId(request);
        final Integer projectId = HttpRequestUtil.getProjectId(request);
        if (StringUtils.isNotBlank(operator)){
            tuple3.update1(operator);
        }
        if (Objects.nonNull(operatorId)&&operatorId>0){
            tuple3.update2(operatorId);
        }
         if (Objects.nonNull(projectId)&&projectId>0){
            tuple3.update3(projectId);
        }
        return tuple3;
        
    }
    
}