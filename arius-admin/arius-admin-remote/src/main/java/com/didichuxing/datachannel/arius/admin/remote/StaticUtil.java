package com.didichuxing.datachannel.arius.admin.remote;

public class StaticUtil {

    /**
     * 构造odin节点
     */
    public static String namespace(String role, String cluster, String nsTree) {
        return String.format("%s.%s.%s", role, cluster, nsTree);
    }
}