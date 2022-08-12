package com.didiglobal.logi.op.manager.infrastructure.common.enums;

/**
 * @author didi
 * @date 2022-07-12 5:00 下午
 */
public enum OperationEnum {
    INSTALL(0, "组件安装"),
    EXPAND(1, "组件扩缩"),
    SHRINK(2, "组件缩容"),
    SCALE(3, "组件扩缩容"),
    CONFIG_CHANGE(4, "配置变更"),
    RESTART(5, "重启"),
    UPGRADE(6, "升级"),
    STATUS(7, "状态"),
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
