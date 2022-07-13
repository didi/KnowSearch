package com.didichuxing.datachannel.arius.admin.common.bean.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PagingData<T> implements Serializable {

    private static final long serialVersionUID = -4498978062649547459L;
    private List<T>           bizData;

    private Pagination        pagination;

    public PagingData(List<T> bizData, long total, long pageNo, long pageSize) {
        this.bizData = bizData;
        this.pagination = new Pagination(total, pageNo, pageSize);
    }

    @Data
    public static class Pagination implements Serializable {

        private static final long serialVersionUID = 1037592182089929607L;
        private long              total;
        private long              pageNo;
        private long              pageSize;

        public Pagination(long total, long pageNo, long pageSize) {
            this.total = total;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
        }
    }

}
