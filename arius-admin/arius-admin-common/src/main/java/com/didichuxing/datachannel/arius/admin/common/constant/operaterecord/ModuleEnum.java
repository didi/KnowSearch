package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 操作记录模块枚举
 *
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum ModuleEnum {

                        /** 模板 */
                        TEMPLATE(1, "模板"),

                        APP(2, "应用"),
                        ES_USER(27, "es user"),

                        WORK_ORDER(3, "工单"),

                        DSL_ANALYZER(4, "DSL审核"),

                        CLUSTER(5, "集群"),

                        CONFIG(6, "通用配置"),

                        RESOURCE_CONFIG(7, "集群资源配置"),

                        SCHEDULE(8, "定时任务"),

                        ARIUS_WORK_ORDER(9, "Arius工单"),

                        MAPPING_SETTINGS(10, "mapping配置"),

                        APP_CONFIG(11, "应用配置"),

                        RESOURCE(12, "资源"),

                        TEMPLATE_CONFIG(13, "模板配置"),

                        SENSE_OP(14, "sense操作"),

                        CAPACITY_PLAN_AREA(15, "容量规划集群"),

                        CAPACITY_PLAN_REGION(16, "容量规划region"),

                        CAPACITY_PLAN_TASK(17, "容量规划任务"),

                        RESOURCE_ORDER(18, "集群创建工单"),

                        PLATFORM_OP(19, "平台运维"),

                        CLUSTER_REGION(20, "物理集群Region"),

                        LOGIC_CLUSTER_PERMISSIONS(30, "逻辑集群权限"),

                        LOGIC_TEMPLATE_PERMISSIONS(31, "逻辑模板权限"),

                        ES_CLUSTER_CONFIG(21, "ES集群配置"),

                        REGION(22, "region"),

                        ES_CLUSTER_PLUGINS(23, "ES集群插件"),

                        ES_CLUSTER_JOIN(24, "集群接入"),

                        INDEX_OP(25, "索引管理"),

                        INDEX_BLOCK_SETTING(26, "索引阻塞配置"),

                        UNKNOWN(-1, "unknown");

    ModuleEnum(int code, String desc) {
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("code", code);
        map.put("desc", desc);
        return map;
    }

    public static ModuleEnum valueOf(Integer code) {
        if (code == null) {
            return ModuleEnum.UNKNOWN;
        }
        for (ModuleEnum state : ModuleEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return ModuleEnum.UNKNOWN;
    }

    public static boolean validate(Integer code) {
        if (code == null) {
            return false;
        }
        for (ModuleEnum state : ModuleEnum.values()) {
            if (state.getCode() == code) {
                return true;
            }
        }

        return false;
    }

    public static List<Map<String, Object>> getAllAriusConfigs() {
        List<Map<String, Object>> objects = Lists.newArrayList();

        for (ModuleEnum moduleEnum : ModuleEnum.values()) {
            if (moduleEnum.getCode() == -1) {
                continue;
            }

            objects.add(moduleEnum.toMap());
        }

        return objects;
    }

}