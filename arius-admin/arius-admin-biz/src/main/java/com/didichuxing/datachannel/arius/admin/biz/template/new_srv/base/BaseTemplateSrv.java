package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public interface BaseTemplateSrv {

    /**
     * 判断指定逻辑模板是否开启了当前模板服务
     * @param templateId 逻辑模板id
     * @return 校验结果
     */
    boolean isTemplateSrvOpen(Integer templateId);

    /**
     * 获取当前模板服务的类型
     * @return 指定的模板服务类型
     */
    NewTemplateSrvEnum templateSrv();

    /**
     * 获取当前模板服务的名称
     * @return
     */
    String templateSrvName();

    /**
     * 开启指定逻辑模板的模板服务
     * @param templateIdList
     * @return
     */
    Result<Void> openSrv(List<Integer> templateIdList) throws AdminOperateException;

    /**
     * 关闭指定逻辑模板的模板服务
     * @param templateIdList
     * @return
     */
    Result<Void> closeSrv(List<Integer> templateIdList) throws AdminOperateException ;

}
