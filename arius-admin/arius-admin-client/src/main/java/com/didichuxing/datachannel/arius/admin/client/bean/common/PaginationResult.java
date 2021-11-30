package com.didichuxing.datachannel.arius.admin.client.bean.common;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "分页结果")
public class PaginationResult<T> extends BaseResult{

    protected PagingData<T> data;

    @Override
    public boolean isPagine() {
        return true;
    }

    public PaginationResult() {
    }

    public PaginationResult(PagingData<T> data) {
        this.data = data;
    }

    public PaginationResult(List<T> records, long total, long pageNo, long pageSize) {
        this.data = new PagingData<>(records, total, pageNo, pageSize);
    }

    public static <T> PaginationResult<T> buildSucc() {
        PaginationResult<T> result = new PaginationResult<>();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        return result;
    }

    public static <T> PaginationResult<T> buildSucc(List<T> records, long total, long pageNo, long pageSize) {
        PaginationResult<T> paginationResult = new PaginationResult<>(records, total, pageNo, pageSize);
        paginationResult.setCode(ResultType.SUCCESS.getCode());
        paginationResult.setMessage(ResultType.SUCCESS.getMessage());
        return paginationResult;
    }

    public static <T> PaginationResult<T> buildFail(String failMsg) {
        PaginationResult<T> result = new PaginationResult<>();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(failMsg);
        return result;
    }

    public static <T> PaginationResult<T> buildParamIllegal(String msg) {
        PaginationResult<T> result = new PaginationResult<>();
        result.setCode(ResultType.ILLEGAL_PARAMS.getCode());
        result.setMessage(ResultType.ILLEGAL_PARAMS.getMessage() + ":" + msg + "，请检查后再提交！");
        return result;
    }
}
