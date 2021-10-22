package com.didichuxing.datachannel.arius.admin.biz.template.srv.base;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseTemplateSrv {

    protected static final ILog               LOGGER = LogFactory.getLog(BaseTemplateSrv.class);

    @Autowired
    protected ESClusterPhyService               esClusterPhyService;

    @Autowired
    protected ESClusterLogicService             esClusterLogicService;

    @Autowired
    protected TemplatePhyService                templatePhyService;

    @Autowired
    protected TemplateLogicService              templateLogicService;

    @Autowired
    protected OperateRecordService              operateRecordService;

    @Autowired
    protected TemplateSrvManager                templateSrvManager;

    @Autowired
    protected TemplatePhyManager                templatePhyManager;

    /**
     * 判断指定物理集群是否开启了当前索引服务
     * @param phyClusterName 物理集群名字
     * @return
     */
    public boolean isTemplateSrvOpen(String phyClusterName) {
        boolean enable = templateSrvManager.isPhyClusterOpenTemplateSrv(phyClusterName, templateService().getCode());

        LOGGER.info("class=BaseTemplateSrv||method=enableTemplateSrv||clusterName={}||enable={}||templateSrv={}",
            phyClusterName, enable, templateServiceName());

        return enable;
    }

    /**
     * 判断物理模板已经开启了当前索引服务（判断指定物理模板所在物理集群是否开启了当前索引服务）
     * @param indexTemplatePhies 物理模板
     * @return
     */
    public boolean isTemplateSrvOpen(List<IndexTemplatePhy> indexTemplatePhies) {
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhies) {
            if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
                return false;
            }
        }

        return true;
    }

    public String templateServiceName() {
        return templateService().getServiceName();
    }

    public abstract TemplateServiceEnum templateService();
}
