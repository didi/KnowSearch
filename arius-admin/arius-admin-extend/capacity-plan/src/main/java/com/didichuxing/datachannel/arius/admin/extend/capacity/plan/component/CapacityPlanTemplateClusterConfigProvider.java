package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.TemplateClusterConfigProvider;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanRegion;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.service.CapacityPlanRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

@Component("capacityPlanTemplateResourceConfigProvider")
public class CapacityPlanTemplateClusterConfigProvider implements TemplateClusterConfigProvider {

    private static final ILog         LOGGER = LogFactory.getLog( CapacityPlanTemplateClusterConfigProvider.class);

    @Autowired
    private CapacityPlanRegionService capacityPlanRegionService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    /**
     * 获取模板所在资源的配置
     *
     * @param physicalId 物理模板的id
     * @return 配置
     */
    @Override
    public Result<TemplateResourceConfig> getTemplateResourceConfig(Long physicalId) {

        CapacityPlanRegion region = capacityPlanRegionService
            .getRegionOfPhyTemplate( indexTemplatePhyService.getTemplateById(physicalId));

        if (region == null) {
            LOGGER.warn("class=CapacityPlanTemplateClusterConfigProvide||method=getTemplateResourceConfig||msg=not match any region");
            return Result.build(ResultType.NO_CAPACITY_PLAN);
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(region.getConfig(), TemplateResourceConfig.class));
    }

}
