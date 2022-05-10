package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

public interface TemplateSrvManager {

    /**
     * 判断指定逻辑模板是否开启了该模板服务
     * @param logicTemplateId 逻辑模板id, templateSrvId 模板服务id
     * @return
     */
    public boolean isTemplateSrvOpen(Integer logicTemplateId, Integer templateSrvId);
}
