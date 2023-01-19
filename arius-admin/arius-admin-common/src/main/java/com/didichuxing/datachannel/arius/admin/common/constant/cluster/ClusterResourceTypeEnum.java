package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 集群资源类型
 * @author ohushenglin_v
 * @date 2022-05-25
 */
public enum ClusterResourceTypeEnum {
                                     PUBLIC(1, "共享集群"),

                                     PRIVATE(2, "独立集群"),

                                     EXCLUSIVE(3, "独享集群"),

                                     UNKNOWN(-1, "未知");

    ClusterResourceTypeEnum(int code, String desc) {
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

    public static ClusterResourceTypeEnum valueOf(Integer code) {
        if (code == null) {
            return ClusterResourceTypeEnum.UNKNOWN;
        }
        for (ClusterResourceTypeEnum state : ClusterResourceTypeEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return ClusterResourceTypeEnum.UNKNOWN;
    }

    public static boolean isExist(Integer code) {
        return UNKNOWN.getCode() != ClusterResourceTypeEnum.valueOf(code).getCode();
    }
    
    public static List<TupleTwo<Integer,ClusterResourceTypeEnum>> getAll() {
        return Arrays.stream(ClusterResourceTypeEnum.values()).map(i -> Tuples.of(i.getCode(), i)).collect(
                Collectors.toList());
    }
}