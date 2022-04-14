package com.didichuxing.datachannel.arius.admin.common.constant.index;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lyn
 * @date 2021/09/30
 **/
public enum IndexStatusEnum {

                             UNKNOWN("unknown"), GREEN("green"),

                             YELLOW("yellow"),

                             RED("red");

    private String status;

    IndexStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static boolean isStatusExit(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }

        for (IndexStatusEnum value : IndexStatusEnum.values()) {
            if (UNKNOWN.getStatus().equals(status)) {
                continue;
            }

            if (status.equals(value.getStatus())) {
                return true;
            }
        }

        return false;
    }

}
