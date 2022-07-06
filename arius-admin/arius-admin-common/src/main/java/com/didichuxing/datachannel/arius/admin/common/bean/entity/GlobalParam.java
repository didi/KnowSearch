package com.didichuxing.datachannel.arius.admin.common.bean.entity;

public class GlobalParam {

    private GlobalParam(){}

    public static ThreadLocal<String>  CURRENT_USER       = new InheritableThreadLocal<>();
    public static ThreadLocal<Integer> CURRENT_PROJECT_ID = new InheritableThreadLocal<>();

}