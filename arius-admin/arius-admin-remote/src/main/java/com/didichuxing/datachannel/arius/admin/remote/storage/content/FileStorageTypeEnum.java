package com.didichuxing.datachannel.arius.admin.remote.storage.content;

/**
 * 文件存储枚举
 *
 * @author linyunan
 * @date 2021-04-26
 */
public enum FileStorageTypeEnum {
                                 /**
                                  * 本地部门信息, 固定为一个部门即可
                                  */
                                 DEFAULT(1, "defaultFileStorage"),

                                 S3(2, "s3FileStorage"),

                                 GIFT(3, "GiftFileStorageHandle"),

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

    public static FileStorageTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return FileStorageTypeEnum.UNKNOWN;
        }
        for (FileStorageTypeEnum codeEnum : FileStorageTypeEnum.values()) {
            if (code.equals(codeEnum.getCode())) {
                return codeEnum;
            }
        }

        return FileStorageTypeEnum.UNKNOWN;
    }
}
