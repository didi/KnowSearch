package com.didichuxing.datachannel.arius.admin.biz.user;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.EditUserPasswordDTO;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;

/**
 * Created by linyunan on 2021-06-22
 */
public interface AriusUserInfoManager {

	/**
	 * 获取用户列表
	 */
	List<AriusUserInfoVO> listUserVOS();

	/**
	 * 校验用户名称合法性
	 */
	Result<Void> checkUserNameValid(String userName);

	/**
	 * 编辑用户信息, 不更新密码
	 */
	Result<Void> editUsersInfoVO(AriusUserInfoDTO ariusUserInfoDTO);

	/**
	 * 更新用户密码
	 */
	Result<Void> editUsersPassword(EditUserPasswordDTO editUserPasswordDTO);

	/**
	 * 获取单个用户信息
	 */
	Result<AriusUserInfoVO> getAriusUserInfoVO(String domainAccount);
}
