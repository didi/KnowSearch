package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengxiang
 * @date 2022/5/9
 */
@Service("newTemplateSrvService")
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {

    protected static final ILog LOGGER = LogFactory.getLog(TemplateSrvManagerImpl.class);
    private final Map<Integer, BaseTemplateSrv> BASE_TEMPLATE_SRV_MAP = new HashMap<>();

    @Autowired
    private IndexTemplateService templateLogicService;

    @PostConstruct
    public void init() {
        Map<String, BaseTemplateSrv> strTemplateSrvHandleMap = SpringTool.getBeansOfType(BaseTemplateSrv.class);
        strTemplateSrvHandleMap.forEach((k, v) -> {
            try {
                NewTemplateSrvEnum srvEnum = v.templateSrv();
                BASE_TEMPLATE_SRV_MAP.put(srvEnum.getCode(), v);
            } catch (Exception e) {
                LOGGER.error("class=TemplateSrvManagerImpl||method=init||error=", e);
            }
        });
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||init finish");
    }

    @Override
    public Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId) {
        try {
            IndexTemplate template = templateLogicService.getLogicTemplateById(logicTemplateId);
            return getTemplateOpenSrv(template);
        } catch (Exception e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=getTemplateOpenSrv||logicTemplateId={}", logicTemplateId, e);
            return Result.buildFail( "获取模板开启服务失败");
        }
    }

    @Override
    public boolean isTemplateSrvOpen(Integer logicTemplateId, Integer templateSrvId) {
        Result<List<TemplateSrv>> openSrvResult = getTemplateOpenSrv(logicTemplateId);
        if (openSrvResult.failed()) {
            return false;
        }

        List<TemplateSrv> openSrv = openSrvResult.getData();
        for (TemplateSrv srv : openSrv) {
            if (templateSrvId.equals(srv.getServiceId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Result<List<TemplateSrv>> getTemplateUnavailableSrv(Integer logicTemplateId) {
        List<TemplateSrv> unavailableSrv = new ArrayList<>();
        List<NewTemplateSrvEnum> allSrvList = NewTemplateSrvEnum.getAll();
        for (NewTemplateSrvEnum srvEnum : allSrvList) {
            Integer srvCode = srvEnum.getCode();
            BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
            Result<Void> availableResult = srvHandle.isTemplateSrvAvailable(logicTemplateId);
            if (availableResult.failed()) {
                unavailableSrv.add(getSrvByCode(srvCode));
            }
        }
        return Result.buildSucc(unavailableSrv);
    }


    //////////////////////////////////private method/////////////////////////////////////////////

    private TemplateSrv getSrvByCode(Integer templateSrvCode) {
        NewTemplateSrvEnum serviceEnum = NewTemplateSrvEnum.getByCode(templateSrvCode);
        return new TemplateSrv(serviceEnum.getCode(), serviceEnum.getServiceName(), serviceEnum.getEsClusterVersion().getVersion());
    }

    private Result<List<TemplateSrv>> getTemplateOpenSrv(IndexTemplate template) {
        if (null == template) {
            return Result.buildNotExist("逻辑模板不存在");
        }

        String openSrvs = template.getOpenSrv();
        if (StringUtils.isBlank(openSrvs)) {
            return Result.buildSucc(new ArrayList<>(), "模板未开启任何服务");
        }

        List<TemplateSrv> templateOpenSrv = new ArrayList<>();
        for(String srvId : openSrvs.split(",")) {
            TemplateSrv templateSrv = getSrvByCode(Integer.parseInt(srvId));
            if (null != templateSrv) {
                templateOpenSrv.add(templateSrv);
            }
        }
        return Result.buildSucc(templateOpenSrv);
    }


}
