package com.didi.cloud.fastdump.common.bean.adapter;

import java.io.Serializable;

/**
 * Created by linyunan on 2022/10/10
 */
public class FastDumpBulkInfo implements Serializable {
    /**
     * bulk 的起始点
     */
    private Integer startPointer;

    public Integer getStartPointer() {
        return startPointer;
    }

    public void setStartPointer(Integer startPointer) {
        this.startPointer = startPointer;
    }

    public Integer getEndPointer() {
        return endPointer;
    }

    public void setEndPointer(Integer endPointer) {
        this.endPointer = endPointer;
    }

    /**
     * bulk 的终止点
     */
    private Integer endPointer;
    private Integer bulkDocNum;
    private String  bulkDocFlatToString;
    /**
     * index type
     */
    private String  sinkerIndexType;

    public String getReaderIndexType() {
        return readerIndexType;
    }

    public void setReaderIndexType(String readerIndexType) {
        this.readerIndexType = readerIndexType;
    }

    private String  readerIndexType;

    public String getSinkerIndexType() {
        return sinkerIndexType;
    }

    public void setSinkerIndexType(String sinkerIndexType) {
        this.sinkerIndexType = sinkerIndexType;
    }

    public String getBulkDocFlatToString() {
        return bulkDocFlatToString;
    }

    public void setBulkDocFlatToString(String bulkDocFlatToString) {
        this.bulkDocFlatToString = bulkDocFlatToString;
    }

    public Integer getBulkDocNum() {
        return bulkDocNum;
    }

    public void setBulkDocNum(Integer bulkDocNum) {
        this.bulkDocNum = bulkDocNum;
    }
}
