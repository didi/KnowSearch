package com.didichuxing.datachannel.arius.admin.common.constant.index;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lyn
 * @date 2021/09/30
 **/
public enum IndexBlockEnum {

                            UNKNOWN("unknown"),

                            READ("read"),

                            WRITE("write");

    private String type;

    IndexBlockEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static boolean isIndexBlockExit(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }

        for (IndexBlockEnum value : IndexBlockEnum.values()) {
            if (UNKNOWN.getType().equals(type)) {
                continue;
            }

            if (type.equals(value.getType())) {
                return true;
            }
        }

        return false;
    }

}
