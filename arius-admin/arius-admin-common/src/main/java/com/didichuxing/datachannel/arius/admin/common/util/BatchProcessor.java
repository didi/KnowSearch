package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;

/**
 * 分配次执行
 * batchList 需要处理的对象List
 * function 处理函数
 * batchSize 批次大小
 * @author d06679
 */
public class BatchProcessor<S, R> {

    private Collection<S>        batchList;

    private Function<List<S>, R> processor;

    @Nullable
    private Predicate<R>         succChecker;

    private Integer              batchSize = 100;

    public BatchProcessor<S, R> batchList(Collection<S> batchList) {
        this.batchList = batchList;
        return this;
    }

    public BatchProcessor<S, R> processor(Function<List<S>, R> processor) {
        this.processor = processor;
        return this;
    }

    public BatchProcessor<S, R> succChecker(Predicate<R> succChecker) {
        this.succChecker = succChecker;
        return this;
    }

    public BatchProcessor<S, R> batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public BatchProcessResult<S, R> process() {

        BatchProcessResult<S, R> result = new BatchProcessResult<>();

        if (CollectionUtils.isEmpty(batchList) || processor == null) {
            return result;
        }

        List<S> tempList = new ArrayList<>(batchSize);
        for (S t : batchList) {
            if (tempList.size() >= batchSize) {
                innerProcess(tempList, result);
                tempList.clear();
            }
            tempList.add(t);
        }

        if (CollectionUtils.isNotEmpty(tempList)) {
            innerProcess(tempList, result);
        }

        return result;
    }

    private void innerProcess(List<S> tempList, BatchProcessResult<S, R> result) {
        try {
            R r = processor.apply(tempList);

            if (succChecker != null) {
                boolean succ = succChecker.test(r);
                if (!succ) {
                    result.addFail(tempList);
                } else {
                    result.addResult(r);
                }
            } else {
                result.addResult(r);
            }

        } catch (Exception e) {
            result.addError(tempList, e);
        }
    }

    public static class BatchProcessResult<S, R> {

        private final List<R>                 resultList = Lists.newArrayList();

        private final List<S>                 failList   = Lists.newArrayList();

        private final Map<List<S>, Exception> errorMap   = Maps.newHashMap();

        void addFail(List<S> fails) {
            this.failList.addAll(fails);
        }

        void addError(List<S> errors, Exception e) {
            this.errorMap.put(errors, e);
        }

        void addResult(R result) {
            this.resultList.add(result);
        }

        public List<R> getResultList() {
            return resultList;
        }

        public List<S> getFailList() {
            return failList;
        }

        public Map<List<S>, Exception> getErrorMap() {
            return errorMap;
        }

        public boolean isSucc() {
            return failList.isEmpty() && errorMap.size() == 0;
        }

        public int getFailAndErrorCount() {
            return failList.size() + errorMap.size();
        }

        @Override
        public String toString() {
            if (isSucc()) {
                return "BatchProcessResult{process succ}";
            }

            return "BatchProcessResult{" + "failSize=" + failList.size() + ", errorSize=" + errorMap.size()
                   + ", errorList=" + getErrorDesc(errorMap) + '}';
        }

        private String getErrorDesc(Map<List<S>, Exception> errorMap) {
            return String.join(",", Lists.newArrayList(errorMap.values().stream()
                .map(e -> e.getClass().getSimpleName() + "_" + e.getMessage()).collect(Collectors.toList())));
        }
    }

}
