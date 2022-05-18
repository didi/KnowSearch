package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateWithSrvConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateWithSrvVO;

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
    Result<List<TemplateSrv>> getTemplateUnavailableSrv(Integer logicTemplateId);

    /**
     * 分页模糊查询模板服务
     * @param condition
     * @return
     */
    PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(TemplateWithSrvConditionDTO condition);

}
