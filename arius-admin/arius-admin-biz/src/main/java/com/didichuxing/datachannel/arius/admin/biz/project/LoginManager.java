package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.exception.OperateForbiddenException;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.dto.account.AccountLoginDTO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录管理器
 *
 * @author shizeying
 * @date 2022/06/16
 */
public interface LoginManager {

    /**
     * 验证登录信息（验证前密码先用Base64解码再用RSA解密） 登录前会检查账户激活状态
     *
     * @param loginDTO 登陆信息
     * @param request  请求信息
     * @return token
     * @throws LogiSecurityException 登录错误
     */
    Result<UserBriefVO> verifyLogin(AccountLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response);

    /**
     * 登出接口
     *
     * @param request
     * @param response
     * @return
     */
    Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 拦截器检查 检查登陆
     *
     * @param request             请求
     * @param response            响应
     * @param requestMappingValue 请求映射value
     * @param whiteMappingValues  白名单
     * @return boolean
     * @throws IOException ioexception
     */
    boolean interceptorCheck(HttpServletRequest request, HttpServletResponse response, String requestMappingValue,
                             List<String> whiteMappingValues) throws IOException, OperateForbiddenException;
}