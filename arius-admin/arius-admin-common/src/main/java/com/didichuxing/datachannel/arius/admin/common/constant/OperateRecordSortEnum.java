package com.didichuxing.datachannel.arius.admin.common.constant;

import java.util.Arrays;
import java.util.Objects;

/**
 * 操作记录分类枚举
 *
 * @author shizeying
 * @date 2022/06/28
 */
public enum OperateRecordSortEnum {
                                   /**
                                    * 操作时间
                                    */
                                   OPERATE_TIME("operate_time", "operateTime"),
                                   /**
                                    * 操作记录id
                                    */
                                   OPERATE_RECORD_ID("id", "id");

    private String sortField;
    private String sortTerm;

    OperateRecordSortEnum(String sortField, String sortTerm) {
        this.sortField = sortField;
        this.sortTerm = sortTerm;
    }

    public String getSortField() {
        return sortField;
    }

    public String getSortTerm() {
        return sortTerm;
    }

    public static String getSortField(String sortTerm) {
        if (Objects.isNull(sortTerm)) {
            return OPERATE_RECORD_ID.getSortField();
        }
        return Arrays.stream(OperateRecordSortEnum.values())
            .filter(operateRecordSortEnum -> operateRecordSortEnum.getSortTerm().equals(sortTerm)).findFirst()
            .map(OperateRecordSortEnum::getSortField).orElse(OPERATE_RECORD_ID.getSortField());
    }

}