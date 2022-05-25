package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 项目关联资源判断
 *
 * @author shizeying
 * @date 2022/05/25
 */
@Component
public class ProjectManagerImpl implements ProjectManager {
	@Autowired
    private ClusterLogicService  clusterLogicService;
	 @Autowired
    private IndexTemplateService indexTemplateService;
	@Override
	public Result<Void> hasOwnLogicClusterByProject(Integer projectId) {
		List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByAppId(projectId);
        return CollectionUtils.isNotEmpty(clusterLogics)?Result.build(ResultType.IN_USE_ERROR.getCode(),
		        "应用申请了集群，不能删除"):Result.buildSucc();
	}
	
	@Override
	public Result<Void> hasOwnTemplateByProjectId(Integer projectId) {
	 List<IndexTemplate> templateLogics = indexTemplateService.getAppLogicTemplatesByAppId(projectId);
        return CollectionUtils.isNotEmpty(templateLogics)?Result.build(ResultType.IN_USE_ERROR.getCode(), "APP申请了模板，不能删除"):Result.buildSucc();
    }

    
}