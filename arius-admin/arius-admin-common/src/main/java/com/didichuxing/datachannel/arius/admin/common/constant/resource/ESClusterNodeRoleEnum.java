package com.didichuxing.datachannel.arius.admin.common.constant.resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 节点角色类型枚举
 *
 * Created by d06679 on 2017/7/14.
 */
public enum ESClusterNodeRoleEnum {
                                   /**datanode*/
                                   DATA_NODE(1, "datanode"),

                                   CLIENT_NODE(2, "clientnode"),

                                   MASTER_NODE(3, "masternode"),

                                   UNKNOWN(-1, "unknown");

    ESClusterNodeRoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ESClusterNodeRoleEnum valueOf(Integer code) {
        if (code == null) {
            return ESClusterNodeRoleEnum.UNKNOWN;
        }
        for (ESClusterNodeRoleEnum typeEnum : ESClusterNodeRoleEnum.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }

        return ESClusterNodeRoleEnum.UNKNOWN;
    }

    public static ESClusterNodeRoleEnum getByDesc(String desc) {
        if (desc == null) {
            return ESClusterNodeRoleEnum.UNKNOWN;
        }
        for (ESClusterNodeRoleEnum typeEnum : ESClusterNodeRoleEnum.values()) {
            if (typeEnum.getDesc().equals(desc)) {
                return typeEnum;
            }
        }

        return ESClusterNodeRoleEnum.UNKNOWN;
    }

    /**
     * 获取去除node部分的role列表，例如data,master等
     */
    public static List<String> nodeRoleList() {
        return Arrays.stream(values()).
                filter(param -> param != UNKNOWN).
                map(ESClusterNodeRoleEnum::getDesc).
                map(roleNode -> roleNode.substring(0, roleNode.lastIndexOf("n")))
                .collect(Collectors.toList());
    }

}
