package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

import java.util.List;

/**
 * 基础分页处理器, 基于模板设计模式实现的一套流水线式编码规范, 后续其他需要分页的业务可以继承此类
 *
 * Created by linyunan on 2021-10-14
 */
public abstract class BasePageSearchHandle<T> implements BaseHandle {

    /**
     * 处理模糊分页查询
     * @param pageDTO     查询条件
     * @param authType    权限
     * @param projectId       项目
     * @return            PaginationResult<T>
     */
    public PaginationResult<T> doPageHandle(PageDTO pageDTO, Integer authType, Integer projectId) {
        Result<Boolean> validCheckForProjectIdResult = validCheckForProjectId(projectId);
        if (validCheckForProjectIdResult.failed()) {
            return PaginationResult.buildParamIllegal(validCheckForProjectIdResult.getMessage());
        }

        Result<Boolean> validCheckForConditionResult = validCheckForCondition(pageDTO, projectId);
        if (validCheckForConditionResult.failed()) {
            return PaginationResult.buildParamIllegal(validCheckForConditionResult.getMessage());
        }

        init(pageDTO);

        // 根据权限类型获取数据，获取APP管理或访问或无权限其中一种权限类型的资源数据
        // 这里涉及权限表和其他业务table多个字段的条件查询，需要放置在内存中去做条件查询和分页处理
        if (null != authType) {
            return buildWithAuthType(pageDTO, authType, projectId);
        }

        // 不区分权限类型，直接获取全量数据，包括管理、访问、无权限等资源数据
        return buildWithoutAuthType(pageDTO, projectId);
    }

    /**
     * 校验项目合法性
     *
     * @param projectId 项目
     * @return Result<Boolean>
     */
    protected abstract Result<Boolean> validCheckForProjectId(Integer projectId);

    /**
     * 校验模糊查询的实体参数合法性
     *
     * @param pageDTO 带分页信息的条件查询实体
     * @param projectId
     * @return Result<Boolean>
     */
    protected abstract Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer projectId);

    /**
     * 初始化，处理某些特殊场景中的参数
     *
     * @param pageDTO
     */
    protected abstract void init(PageDTO pageDTO);

    /**
     * 根据权限和其他条件获取模糊查询结果
     *
     * @param pageDTO 带分页信息的条件查询实体
     * @param projectId   项目
     * @return pageVO    需要构建的分页结果
     */
    protected abstract PaginationResult<T> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer projectId);

    /**
     * 获取不带权限条件模糊查询结果
     *
     * @param pageDTO 带分页信息的条件查询实体
     * @param projectId   项目
     * @return pageVO   需要构建的分页结果
     */
    protected abstract PaginationResult<T> buildWithoutAuthType(PageDTO pageDTO, Integer projectId);

    /**
     * 获取最后一条数据的index，以防止数组溢出
     * @param condition       分页条件
     * @param pageSizeFromDb  查询结果
     * @return
     */
    long getLastPageSize(PageDTO condition, Integer pageSizeFromDb) {
        //分页最后一条数据的index
        long size = condition.getPage() * condition.getSize();
        if (pageSizeFromDb < size) {
            size = pageSizeFromDb;
        }
        return size;
    }

    /**
     * 对全量查询结果根据分页条件进行过滤
     * @param condition 分页条件
     * @param source 全量查询结果
     * @return
     */
    <T> List<T> filterFullDataByPage(List<T> source, PageDTO condition) {
        //这里页码和前端对应起来，第一页页码是1 而不是0
        long fromIndex = condition.getSize() * (condition.getPage() - 1);
        long toIndex = getLastPageSize(condition, source.size());
        return source.subList((int) fromIndex, (int) toIndex);
    }
}