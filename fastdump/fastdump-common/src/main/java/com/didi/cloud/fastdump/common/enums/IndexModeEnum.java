package com.didi.cloud.fastdump.common.enums;

/**
 * 索引模式，等同于 es中的 op_type
 * CREATE 新增 如果指定id的文档已经存在，则抛异常
 * INSERT 插入 如果指定id的文档已经存在，覆盖
 * UPDATE 旧版本文档会覆盖新版本文档
 */
public enum IndexModeEnum {
    CREATE("create"),
    INSERT("insert"),
    UPDATE("update");
    private final String mode;

    IndexModeEnum(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public static IndexModeEnum findModeEnum(String mode) {
        for (IndexModeEnum modeEnum : IndexModeEnum.values()) {
            if (mode.equals(modeEnum.getMode())) {
                return modeEnum;
            }
        }
        return CREATE;
    }
}
