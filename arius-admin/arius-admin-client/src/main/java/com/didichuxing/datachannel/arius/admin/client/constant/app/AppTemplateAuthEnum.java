package com.didichuxing.datachannel.arius.admin.client.constant.app;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作枚举
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum AppTemplateAuthEnum {

                                 /**
                                  * Owner权限
                                  */
                                 OWN(1, "own", "管理"),

                                 /**
                                  * 读写权限
                                  */
                                 RW(2, "rw", "读写"),

                                 /**
                                  * 读权限
                                  */
                                 R(3, "r", "读"),

                                 /**
                                  * 没有权限
                                  */
                                 NO_PERMISSION(-1, "", "unknown");

    AppTemplateAuthEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    private final Integer code;
    private final String  name;
    private final String  desc;

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AppTemplateAuthEnum valueOf(Integer code) {
        if (code == null) {
            return AppTemplateAuthEnum.NO_PERMISSION;
        }
        for (AppTemplateAuthEnum state : AppTemplateAuthEnum.values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        
        return AppTemplateAuthEnum.NO_PERMISSION;
    }

    public static AppTemplateAuthEnum valueOfName(String name) {
        if (name == null) {
            return AppTemplateAuthEnum.NO_PERMISSION;
        }

        name = name.toLowerCase();

        if ("w".equals(name)) {
            name = RW.getName();
        }

        for (AppTemplateAuthEnum state : AppTemplateAuthEnum.values()) {
            if (state.getName().equals(name)) {
                return state;
            }
        }

        return AppTemplateAuthEnum.NO_PERMISSION;
    }
    
    public static List<Integer> listAppTemplateAuthCodes() {
        return Arrays.stream(AppTemplateAuthEnum.values())
                     .map(AppTemplateAuthEnum::getCode)
                     .distinct()
                     .collect(Collectors.toList());
    }
}
