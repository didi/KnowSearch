package com.didichuxing.datachannel.arius.admin.client.mapping;

/**
 * 排序类型枚举
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum SortEnum {
                      /**开始doc value*/
                      YES(1, "是"),

                      NO(0, "否"),

                      UNKNOWN(-1, "unknown");

    SortEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;

    private String  desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SortEnum valueFrom(Integer code) {
        if (code == null) {
            return SortEnum.UNKNOWN;
        }
        for (SortEnum state : SortEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }

        return SortEnum.UNKNOWN;
    }

}
