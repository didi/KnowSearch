package com.didichuxing.datachannel.arius.admin.biz.security.resource;

import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.dto.resource.ResourceDTO;
import com.didiglobal.logi.security.extend.ResourceExtend;
import com.didiglobal.logi.security.properties.LogiSecurityProper;
import java.util.Collections;
import java.util.List;
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

        return Collections.emptyList();
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
        return 0;
    }
}