package com.didichuxing.datachannel.arius.admin.remote.storage.content;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * 文件存储枚举
 *
 * @author linyunan
 * @date 2021-04-26
 */
public enum FileStorageTypeEnum {
                                 DEFAULT(1, "default"),

                                 S3(2, "s3"),

                                 GIFT(3, "gift"),

                                 UNKNOWN(-1, "unknown");

    private Integer code;

    private String  type;

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
        if (AriusObjUtils.isNull(type)) {
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
