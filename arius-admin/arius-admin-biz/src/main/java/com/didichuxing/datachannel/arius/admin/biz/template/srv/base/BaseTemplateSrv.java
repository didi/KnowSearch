package com.didichuxing.datachannel.arius.admin.biz.template.srv.base;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseTemplateSrv implements BaseTemplateSrvInterface {

    protected static final ILog    LOGGER = LogFactory.getLog(BaseTemplateSrv.class);

    @Autowired
    protected ClusterPhyService    clusterPhyService;

    @Autowired
    protected ClusterLogicService  clusterLogicService;

    @Autowired
    protected TemplatePhyService   templatePhyService;

    @Autowired
    protected IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    protected OperateRecordService operateRecordService;

    @Autowired
    protected TemplateSrvManager   templateSrvManager;

    @Autowired
    protected TemplatePhyManager   templatePhyManager;


    @Override
    public boolean isTemplateSrvOpen(String phyClusterName) {
        boolean enable = templateSrvManager.isPhyClusterOpenTemplateSrv(phyClusterName, templateService().getCode());

        LOGGER.info("class=BaseTemplateSrv||method=enableTemplateSrv||clusterName={}||enable={}||templateSrv={}",
            phyClusterName, enable, templateServiceName());

        return enable;
    }

    @Override
    public boolean isTemplateSrvOpen(List<IndexTemplatePhy> indexTemplatePhies) {
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhies) {
            if (!isTemplateSrvOpen(indexTemplatePhy.getCluster())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Result<Boolean> checkOpenTemplateSrvByCluster(String phyCluster){
        return Result.buildSucc(Boolean.TRUE);
    }

    @Override
    public Result<Boolean> checkOpenTemplateSrvWhenClusterJoin(String httpAddresses, String password) {
        return Result.buildSucc(Boolean.TRUE);
    }

    @Override
    public String templateServiceName() {
        return templateService().getServiceName();
    }
}
