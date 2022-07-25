package com.didichuxing.datachannel.arius.admin.biz.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

/**
 * @author cjm
 */
public interface DslTemplateManager {

    /**
     * 根据appid修改查询模版限流值
     *
     * @param dslTemplateList dsl模板限流值信息，待修改限流值
     * @return Result<Boolean>
     */
    Result<Boolean> updateDslTemplateQueryLimit(Integer projectId,String operator,List<DslQueryLimitDTO> dslTemplateList);

    /**
     * 更新查询模版的 启用|停用 状态
     *
     * @param projectId          查询模板对应的projectId
     * @param dslTemplateMd5 dsl模板MD5
     * @return Result<Boolean>
     */
    Result<Boolean> changeDslTemplateStatus(Integer projectId,String operator, String dslTemplateMd5);

    /**
     * 根据dslTemplateMd5查找DSL模版详情
     * @param projectId 应用账号
     * @param dslTemplateMd5 dsl模板MD5
     * @return Result<DslTemplateVO>
     */
    Result<DslTemplateVO> getDslTemplateDetail(Integer projectId, String dslTemplateMd5);

    /**
     * 分页获取DSL模版列表信息
     * @param projectId 应用id
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    PaginationResult<DslTemplateVO> getDslTemplatePage(Integer projectId,
                                                       DslTemplateConditionDTO queryDTO) throws NotFindSubclassException;
}