package com.didichuxing.datachannel.arius.admin.biz.page;

import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.ProjectService;

/**
 * @author ohushenglin_v
 * @date 2022-05-27
 */
public abstract class AbstractPageSearchHandle<T extends PageDTO, R> implements BaseHandle {
    protected final ILog     LOGGER = LogFactory.getLog(this.getClass());
    @Autowired
    protected ProjectService projectService;

    /**
     * 处理模糊分页查询
     * @param condition     查询条件
     * @param projectId       项目
     * @return            PaginationResult<R>
     */
    public PaginationResult<R> doPage(T condition, Integer projectId) {
        Result<Boolean> validCheckForConditionResult = baseCheckCondition(condition, projectId);
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
    protected Result<Boolean> baseCheckCondition(T condition, Integer projectId) {
        if (AriusObjUtils.isNull(projectId) || !projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal("项目不存在");
        }
        if (AriusObjUtils.isNull(condition)) {
            return Result.buildParamIllegal("查询参数不存在");
        }
        return checkCondition(condition, projectId);
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