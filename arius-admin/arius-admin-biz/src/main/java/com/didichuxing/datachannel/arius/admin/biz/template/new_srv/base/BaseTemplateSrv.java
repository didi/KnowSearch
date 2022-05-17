package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public interface BaseTemplateSrv {

    /**
     * 判断指定逻辑模板是否开启了当前模板服务
     * @param logicTemplateId 逻辑模板id
     * @return 校验结果
     */
    boolean isTemplateSrvOpen(Integer logicTemplateId);

    /**
     * 获取当前模板服务的类型
     * @return 指定的模板服务类型
     */
    TemplateServiceEnum templateSrv();


    /**
     * 获取当前模板服务的名称
     * @return
     */
    String templateSrvName();

    /**
     * 判断指定逻辑模板是否可以开启当前模板服务
     * @param logicTemplateId 逻辑模板id
     * @return 校验结果
     */
    Result<Void> isTemplateSrvAvailable(Integer logicTemplateId);

}
