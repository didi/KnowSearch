package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;

/**
 * Created by linyunan on 2021-06-15
 */
public interface AppLogicTemplateAuthManager {

	/**
	 * 更新模板权限
	 */
	Result updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator);
}
