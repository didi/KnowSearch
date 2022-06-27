package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;

import java.util.List;

/**
 * Created by linyunan on 2021-06-15
 */
public interface ProjectLogicTemplateAuthManager {

    /**
     * 获取当前项目对模板列表的权限信息
     * @param projectId                    项目
     * @param indexTemplateList   逻辑模板信息列表
     * @return
     */
    List<ProjectTemplateAuth> getTemplateAuthListByTemplateListAndProjectId(Integer projectId,
                                                                            List<IndexTemplate> indexTemplateList);

    /**
     * 更新模板权限
     */
    Result<Void> updateTemplateAuth(ProjectTemplateAuthDTO authDTO, String operator);
}