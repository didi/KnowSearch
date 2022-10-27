package com.didiglobal.logi.op.manager.infrastructure.common.enums;

import org.elasticsearch.common.Strings;

/**
 * @author didi
 * @date 2022-07-05 10:20 上午
 */
public enum FileStorageTypeEnum {
    S3(1, "s3"),
    LOCAL(2, "local"),
    UNKNOWN(-1, "unKnow");

    private Integer code;

    private String type;

    FileStorageTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public static FileStorageTypeEnum valueOfType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            return FileStorageTypeEnum.UNKNOWN;
        }
        for (FileStorageTypeEnum typeEnum : FileStorageTypeEnum.values()) {
            if (type.equals(typeEnum.getType())) {
                return typeEnum;
            }
        }

        return FileStorageTypeEnum.UNKNOWN;
    }


}
