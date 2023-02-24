package com.didi.cloud.fastdump.common.enums;

/**
 * 索引写入映射类型
 */
public enum IndexMapTypeEnum {

                              ALL_TO_ONE("all_to_one"),

                              ONE_TO_ONE("one_to_one"),

                              CUSTOM("custom"),

                              NOT_SUPPORT_TYPE("not support_type");

    private String indexType;

    IndexMapTypeEnum(String indexType) {
        this.indexType = indexType;
    }

    public String getIndexType() {
        return indexType;
    }

    public static IndexMapTypeEnum valueOfByIndexType(String indexType) {
        for (IndexMapTypeEnum indexTypeEnum : IndexMapTypeEnum.values()) {
            if (indexType.equals(indexTypeEnum.getIndexType())) {
                return indexTypeEnum;
            }
        }
        return NOT_SUPPORT_TYPE;
    }
}
