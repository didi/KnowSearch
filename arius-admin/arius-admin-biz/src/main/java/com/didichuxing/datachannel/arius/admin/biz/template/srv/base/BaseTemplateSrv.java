package com.didichuxing.datachannel.arius.admin.biz.template.srv.base;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.template.SupportSrv;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import java.util.List;

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
     *
     * @return 指定的模板服务类型
     */
    TemplateServiceEnum templateSrv();

    /**
     * 获取当前模板服务的名称
     * @return
     */
    String templateSrvName();

    /**
     * 开启指定逻辑模板的模板服务
     *
     * @param templateIdList
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> openSrv(List<Integer> templateIdList, String operator, Integer projectId) throws AdminOperateException;

    /**
     * 关闭指定逻辑模板的模板服务
     *
     * @param templateIdList
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> closeSrv(List<Integer> templateIdList, String operator,
                          Integer projectId) throws AdminOperateException;

    ///////////////////////////////////srv


    /**
     * 判断物理模板已经开启了当前索引服务（判断指定物理模板所在物理集群是否开启了当前索引服务）
     * @param indexTemplatePhies 物理模板
     * @return 校验结果
     */
    boolean isTemplateSrvOpen(List<IndexTemplatePhy> indexTemplatePhies);

    /**
     * 获取当前模板服务在模板服务集合类中的标识
     * @see TemplateServiceEnum
     * @return 模板服务描述
     */
    String templateServiceName();





    SupportSrv getLogicTemplateSupportDCDRAndPipelineByLogicId(Integer logicTemplateId);
}