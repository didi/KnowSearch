package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateSrv;

import java.util.List;

public interface TemplateSrvManager {

    /**
     * 判断指定逻辑模板是否开启了该模板服务
     * @param logicTemplateId 逻辑模板id, templateSrvId 模板服务id
     * @return
     */
    boolean isTemplateSrvOpen(Integer logicTemplateId, Integer templateSrvId);


    /**
     * 获取指定模板开启的服务
     * @param logicTemplateId
     * @return
     */
    Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId);

}
