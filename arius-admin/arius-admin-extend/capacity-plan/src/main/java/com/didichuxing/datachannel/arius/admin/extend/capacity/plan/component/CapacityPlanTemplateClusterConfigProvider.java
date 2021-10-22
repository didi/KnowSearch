package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterConfigProvider;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author d06679
 * @date 2019-07-09
 */
@Component("capacityPlanTemplateResourceConfigProvider")
public class CapacityPlanTemplateClusterConfigProvider implements TemplateClusterConfigProvider {

    private static final ILog         LOGGER = LogFactory.getLog( CapacityPlanTemplateClusterConfigProvider.class);

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private TemplatePhyService templatePhyService;

    /**
     * 获取模板所在资源的配置
     *
     * @param physicalId 物理模板的id
     * @return 配置
     */
    @Override
    public Result<TemplateResourceConfig> getTemplateResourceConfig(Long physicalId) {

        CapacityPlanRegion region = capacityPlanRegionService
            .getRegionOfPhyTemplate( templatePhyService.getTemplateById(physicalId));

        if (region == null) {
            LOGGER.warn("method=getTemplateResourceConfig||msg=not match any region");
            return Result.buildFrom(Result.build(ResultType.NO_CAPACITY_PLAN));
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(region.getConfig(), TemplateResourceConfig.class));
    }

}
