package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;

import java.util.List;

/**
 * Created by linyunan on 2021-06-15
 */
public interface AppLogicTemplateAuthManager {

    /**
     * 获取当前项目对模板列表的权限信息
     * @param appId                    项目
     * @param indexTemplateList   逻辑模板信息列表
     * @return
     */
    List<AppTemplateAuth> getTemplateAuthListByTemplateListAndAppId(Integer appId,
                                                                    List<IndexTemplate> indexTemplateList);

    /**
     * 更新模板权限
     */
    Result<Void> updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator);
}
