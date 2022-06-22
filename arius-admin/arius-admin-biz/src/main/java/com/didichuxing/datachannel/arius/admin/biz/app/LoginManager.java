package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.dto.account.AccountLoginDTO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
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
     * 验证登录信息（验证前密码先用Base64解码再用RSA解密）
     * 登录前会检查账户激活状态
     * @param loginDTO 登陆信息
     * @param request 请求信息
     * @return token
     * @throws LogiSecurityException 登录错误
     */
    Result<UserBriefVO> verifyLogin(AccountLoginDTO loginDTO,
	HttpServletRequest request, HttpServletResponse response);

    /**
     * 登出接口
     * @param request
     * @param response
     * @return
     */
    Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response);
}