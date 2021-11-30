package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

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
     * @param appId       项目
     * @return            PaginationResult<T>
     */
    public PaginationResult<T> doPageHandle(PageDTO pageDTO, Integer authType, Integer appId) {
        Result<Boolean> validCheckForAppIdResult = validCheckForAppId(appId);
        if (validCheckForAppIdResult.failed()) {
            return PaginationResult.buildParamIllegal(validCheckForAppIdResult.getMessage());
        }

        Result<Boolean> validCheckForConditionResult = validCheckForCondition(pageDTO, appId);
        if (validCheckForConditionResult.failed()) {
            return PaginationResult.buildParamIllegal(validCheckForAppIdResult.getMessage());
        }

        init(pageDTO);

        if (null != authType) {
            return buildWithAuthType(pageDTO, authType, appId);
        }

        return buildWithoutAuthType(pageDTO, appId);
    }

    /**
     * 校验项目合法性
     *
     * @param appId 项目
     * @return Result<Boolean>
     */
    protected abstract Result<Boolean> validCheckForAppId(Integer appId);

    /**
     * 校验模糊查询的实体参数合法性
     *
     * @param pageDTO 带分页信息的条件查询实体
     * @param appId
     * @return Result<Boolean>
     */
    protected abstract Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer appId);

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
     * @param appId   项目
     * @return pageVO    需要构建的分页结果
     */
    protected abstract PaginationResult<T> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer appId);

    /**
     * 获取不带权限条件模糊查询结果
     *
     * @param pageDTO 带分页信息的条件查询实体
     * @param appId   项目
     * @return pageVO   需要构建的分页结果
     */
    protected abstract PaginationResult<T> buildWithoutAuthType(PageDTO pageDTO, Integer appId);

    /**
     * 获取最后一页分页数量
     * @param condition       分页条件
     * @param pageSizeFromDb  查询结果
     * @return
     */
    long getLastPageSize(PageDTO condition, Integer pageSizeFromDb) {
        long size = condition.getFrom() + condition.getSize();
        if (pageSizeFromDb < size) {
            size = pageSizeFromDb;
        }
        return size;
    }
}
