package com.didichuxing.datachannel.arius.admin.biz.security.resource;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.ProjectResourceEnum;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.dto.resource.ResourceDTO;
import com.didiglobal.logi.security.extend.ResourceExtend;
import com.didiglobal.logi.security.properties.LogiSecurityProper;
import com.didiglobal.logi.security.service.ResourceTypeService;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * resourceExtend的实现类在spring容器bean的名称， logi-security 中资源权限管理模块，需要获取具体资源的信息， 所以用户需实现 ResourceExtend
 * 接口并指定实现类在spring容器中bean的名称； 当前默认为空的实现的状态，在后期开发中会根据需求进行实现 其中配置为
 *
 * @author shizeying
 * @date 2022/05/23
 * @see LogiSecurityProper#getResourceExtendBeanName()
 * @see ResourceExtend
 */
@Component
public class ResourceExtendManager implements ResourceExtend {
	@Autowired
	private ResourceTypeService  resourceTypeService;
	@Autowired
	private ESUserService        esUserService;
	@Autowired
	private ClusterLogicService  clusterLogicService;
	@Autowired
	private IndexTemplateService indexTemplateService;
	
	/**
	 * 获取资源信息List，资源id指的是该资源所在服务对该资源的标识
	 *
	 * @param projectId      项目id（可为null）
	 * @param resourceTypeId 资源类型id（可为null，不为null则projectId必不为null）
	 * @param resourceName   资源名称（可为null，模糊查询条件）
	 * @param page           当前页（分页条件）
	 * @param size           页大小（分页条件）
	 * @return 资源信息List
	 */
	@Override
	public PagingData<ResourceDTO> getResourcePage(Integer projectId, Integer resourceTypeId, String resourceName,
	                                               int page, int size) {
		return null;
	}
	
	/**
	 * 获取资源信息List，资源id指的是该资源所在服务对该资源的标识
	 *
	 * @param projectId      项目id（可为null）
	 * @param resourceTypeId 资源类型id（可为null，不为null则projectId必不为null）
	 * @return 资源信息List
	 */
	@Override
	public List<ResourceDTO> getResourceList(Integer projectId, Integer resourceTypeId) {
		//校验项目是否绑定es user
		List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));
		List<ResourceDTO> resourceDTOS = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(esUsers)) {
			ResourceDTO resourceDTO = new ResourceDTO();
			resourceDTO.setResourceName((ProjectResourceEnum.PROJECT_ES_USER.getDesc()));
			resourceDTO.setProjectId(projectId);
			resourceDTO.setResourceTypeId(ProjectResourceEnum.PROJECT_ES_USER.getCode());
			resourceDTOS.add(resourceDTO);
		}
		//校验项目绑定逻辑集群
		List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByAppId(projectId);
		if (CollectionUtils.isNotEmpty(clusterLogics)) {
			ResourceDTO resourceDTO = new ResourceDTO();
			resourceDTO.setResourceName((ProjectResourceEnum.PROJECT_CLUSTER_LOGIC.getDesc()));
			resourceDTO.setProjectId(projectId);
			resourceDTO.setResourceTypeId(ProjectResourceEnum.PROJECT_CLUSTER_LOGIC.getCode());
			resourceDTOS.add(resourceDTO);
		}
		
		//校验项目绑定模板服务
		List<IndexTemplate> indexTemplates = indexTemplateService.getProjectLogicTemplatesByProjectId(projectId);
		if (CollectionUtils.isNotEmpty(indexTemplates)) {
			ResourceDTO resourceDTO = new ResourceDTO();
			resourceDTO.setResourceName((ProjectResourceEnum.PROJECT_INDEX_TEMPLATE.getDesc()));
			resourceDTO.setProjectId(projectId);
			resourceDTO.setResourceTypeId(ProjectResourceEnum.PROJECT_CLUSTER_LOGIC.getCode());
			resourceDTOS.add(resourceDTO);
		}
		
		return resourceDTOS;
	}
	
	/**
	 * 获取具体资源个数，资源id指的是该资源所在服务对该资源的标识
	 *
	 * @param projectId      项目id（可为null）
	 * @param resourceTypeId 资源类型id（可为null，不为null则projectId必不为null）
	 * @return 资源信息List
	 */
	@Override
	public int getResourceCnt(Integer projectId, Integer resourceTypeId) {
		return resourceTypeService.getAllResourceTypeList().size();
	}
}