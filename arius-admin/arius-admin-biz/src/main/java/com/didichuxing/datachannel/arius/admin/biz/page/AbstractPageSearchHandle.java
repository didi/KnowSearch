package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author ohushenglin_v
 * @date 2022-05-27
 */
public abstract class AbstractPageSearchHandle<T extends PageDTO, R> implements BaseHandle {
    /**
     * 处理模糊分页查询
     * @param condition     查询条件
     * @param projectId       项目
     * @return            PaginationResult<R>
     */
    public PaginationResult<R> doPage(T condition, Integer projectId) {
        Result<Boolean> validCheckForConditionResult = checkCondition(condition, projectId);
        if (validCheckForConditionResult.failed()) {
            return PaginationResult.buildParamIllegal(validCheckForConditionResult.getMessage());
        }

        initCondition(condition, projectId);

        return buildPageData(condition, projectId);
    }

    /**
     * 校验模糊查询的实体参数合法性
     *
     * @param condition 带分页信息的条件查询实体
     * @param projectId
     * @return Result<Boolean>
     */
    protected abstract Result<Boolean> checkCondition(T condition, Integer projectId);

    /**
     * 初始化条件
     *
     * @param condition 条件
     * @param projectId     应用程序id
     */
    protected abstract void initCondition(T condition, Integer projectId);

    /**
     * 获取模糊查询结果
     *
     * @param condition 带分页信息的条件查询实体
     * @param projectId   项目
     * @return PaginationResult<R>   需要构建的分页结果
     */
    protected abstract PaginationResult<R> buildPageData(T condition, Integer projectId);
}