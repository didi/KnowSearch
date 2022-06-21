package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.LoginManager;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.dto.account.AccountLoginDTO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.service.LoginService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	private LoginService loginService;
	
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
}