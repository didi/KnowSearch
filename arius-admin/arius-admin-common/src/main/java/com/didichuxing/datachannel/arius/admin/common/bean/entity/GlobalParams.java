package com.didichuxing.datachannel.arius.admin.common.bean.entity;

public class GlobalParams {

    private GlobalParams(){}

    public static ThreadLocal<String> CURRENT_USER = new InheritableThreadLocal<>();
    public static ThreadLocal<Integer> CURRENT_APPID = new InheritableThreadLocal<>();

}
