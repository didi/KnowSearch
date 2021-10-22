package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Lists;

/**
 * 分配次执行
 * batchList 需要处理的对象List
 * function 处理函数
 * batchSize 批次大小
 * @author d06679
 */
public class BatchConvert<S, T> {

    private Collection<S>                    batchList;

    private Function<Collection<S>, List<T>> function;

    private Integer                          batchSize = 100;

    public Collection<S> getBatchList() {
        return batchList;
    }

    public BatchConvert<S, T> batchList(Collection<S> batchList) {
        this.batchList = batchList;
        return this;
    }

    public Function<Collection<S>, List<T>> getFunction() {
        return function;
    }

    public BatchConvert<S, T> function(Function<Collection<S>, List<T>> function) {
        this.function = function;
        return this;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public BatchConvert<S, T> batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public List<T> execute() {
        if (CollectionUtils.isEmpty(batchList) || function == null) {
            return Lists.newArrayList();
        }

        List<T> result = Lists.newArrayList();

        List<S> tempList = new ArrayList<>(batchSize);
        for (S t : batchList) {
            tempList.add(t);
            if (tempList.size() >= batchSize) {
                List<T> list = function.apply(tempList);
                if (list != null) {
                    result.addAll(list);
                }
                tempList.clear();
            }
        }

        if (tempList.size() > 0) {
            List<T> list = function.apply(tempList);
            if (list != null) {
                result.addAll(list);
            }
        }

        return result;
    }

}
