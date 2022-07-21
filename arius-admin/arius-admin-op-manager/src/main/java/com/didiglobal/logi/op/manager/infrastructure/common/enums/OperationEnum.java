package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-12 5:00 下午
 */
public enum OperationEnum {
    INSTALL(0, "组件安装"),
    SCALE(1, "组件扩缩容"),
    UN_KNOW(-1, "未知");

    private int type;
    private String describe;

    OperationEnum(int type, String describe) {
        this.type = type;
        this.describe = describe;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public static OperationEnum valueOfType(int type) {
        for (OperationEnum typeEnum : OperationEnum.values()) {
            if (type == typeEnum.getType()) {
                return typeEnum;
            }
        }

        return UN_KNOW;
    }

}
