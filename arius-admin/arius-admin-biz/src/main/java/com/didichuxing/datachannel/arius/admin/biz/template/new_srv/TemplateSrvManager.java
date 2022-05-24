package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.UnavailableTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public interface TemplateSrvManager {

    /**
     * 判断指定逻辑模板是否开启了该模板服务
     * @param logicTemplateId 逻辑模板id
     * @param templateSrvId 模板服务id
     * @return
     */
    boolean isTemplateSrvOpen(Integer logicTemplateId, Integer templateSrvId);


    /**
     * 获取指定模板「开启的」服务
     * @param logicTemplateId 逻辑模板id
     * @return
     */
    Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId);

    /**
     * 获取指定模板「不可用的」服务
     * @param logicTemplateId 逻辑模板id
     * @return
     */
    List<UnavailableTemplateSrv> getUnavailableSrv(Integer logicTemplateId);

    /**
     * 过滤可用「指定模板服务」的模板列表
     * @param srvCode
     * @param logicTemplateIdList 待验证是否可用的模板id列表
     * @return
     */
    Result<List<TemplateWithSrvVO>> checkAvailable(Integer srvCode, List<Integer> logicTemplateIdList);

    /**
     * 分页模糊查询模板服务
     * @param condition
     * @return
     */
    PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(TemplateQueryDTO condition);

    /**
     * 开启模板服务
     * @param srvCode 服务代码
     * @param templateIdList 模板id列表
     * @param openParam 开启参数
     * @return
     */
    Result<Void> openSrv(Integer srvCode, List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam);

    /**
     * 关闭模板服务
     * @param srvCode 服务代码
     * @param templateIdList 模板id列表
     * @return
     */
    Result<Void> closeSrv(Integer srvCode, List<Integer> templateIdList);

}
