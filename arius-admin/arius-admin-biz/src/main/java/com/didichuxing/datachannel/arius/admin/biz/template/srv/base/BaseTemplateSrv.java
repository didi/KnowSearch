package com.didichuxing.datachannel.arius.admin.biz.template.srv.base;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
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
     * @param templateId 逻辑模板id
     * @return 校验结果
     */
    boolean isTemplateSrvOpen(Integer templateId);

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

    /**
     * 根据物理集群名称判断是否可以开启对应的模板服务
     * @param phyCluster 物理集群名称
     * @return 校验结果
     */
    Result<Boolean> checkOpenTemplateSrvByCluster(String phyCluster);

    /**
     * 根绝可以连接的client的http地址列表，逗号隔开，例如127.0.0.1:8060,127.0.0.2:8060
     * @param httpAddresses client地址
     * @param password 集群接入的时候的密码 user:password的形式
     * @return 校验结果
     */
    Result<Boolean> checkOpenTemplateSrvWhenClusterJoin(String httpAddresses, String password);

}