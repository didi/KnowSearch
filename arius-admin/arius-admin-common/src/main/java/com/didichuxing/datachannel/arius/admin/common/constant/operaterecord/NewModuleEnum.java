package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作记录模块枚举
 *
 * @author shizeying
 * @date 2022/06/13
 */
public enum NewModuleEnum {
    /**
     * 物理集群：
     */
    PHYSICAL_CLUSTER("物理集群",0),
    
    /**
     * 我的集群：
     */
    MY_CLUSTER("我的集群",1),
    
    /**
     * 应用：
     */
    APPLICATION("应用",2),
    
    /**
     * 租户
     */
    TENANT("租户",3),
    
    /**
     * 索引模板管理：模板创建
     */
    INDEX_TEMPLATE_MANAGEMENT("索引模板管理",4),
    
    /**
     * 模板服务:DCDR设置
     */
    TEMPLATE_SERVICE("模板服务",5),
    
    /**
     * 索引管理:创建
     */
    INDEX_MANAGEMENT("索引管理",6),
    
    /**
     * 索引服务:
     */
    INDEX_SERVICE("索引服务",7),
    /**
     * 查询模板
     */
    QUERY_TEMPLATE("查询模板",8),
    
    /**
     * 配置
     */
    SETTING("配置",9);
    /**
     * 模块
     */
    private final String module;
    /**
     * code
     */
    private final Integer code;
    
    NewModuleEnum(String module, Integer code) {
        this.module = module;
        this.code = code;
    }
    
    public String getModule() {
        return module;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public static Map<String, Integer> toMap() {
        return Arrays.stream(NewModuleEnum.values())
                .collect(Collectors.toMap( NewModuleEnum::getModule,NewModuleEnum::getCode));
    }
    
   
    
    public static NewModuleEnum getModuleEnum(Integer code) {
        return Arrays.stream(NewModuleEnum.values()).filter(moduleEnum -> moduleEnum.getCode().equals(code))
                .findFirst().orElse(null);
    }
    
}