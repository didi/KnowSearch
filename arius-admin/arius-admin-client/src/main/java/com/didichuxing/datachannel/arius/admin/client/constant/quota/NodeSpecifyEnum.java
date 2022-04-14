package com.didichuxing.datachannel.arius.admin.client.constant.quota;

/**
 * 操作记录模块枚举
 *
 * @author d06679
 * @date 2017/7/14
 * @deprecated
 */
@Deprecated
public enum NodeSpecifyEnum {

                             /**
                              * 弹性云规格
                              */
                             DOCKER(1, "16C32G3T-弹性云", new Resource(16d, 64d, 1024d * 3));

    NodeSpecifyEnum(int code, String desc, Resource resource) {
        this.code = code;
        this.desc = desc;
        this.resource = resource;
    }

    private int      code;

    private String   desc;

    private Resource resource;

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

    public Resource getResource() {
        return resource;
    }

    public static Resource resourceOf(Integer code) {
        if (code == null) {
            return NodeSpecifyEnum.DOCKER.getResource();
        }
        for (NodeSpecifyEnum specifyEnum : NodeSpecifyEnum.values()) {
            if (specifyEnum.getCode() == code) {
                return specifyEnum.getResource();
            }
        }

        return NodeSpecifyEnum.DOCKER.getResource();
    }

    public static String descOf(Integer code) {
        if (code == null) {
            return NodeSpecifyEnum.DOCKER.getDesc();
        }
        for (NodeSpecifyEnum specifyEnum : NodeSpecifyEnum.values()) {
            if (specifyEnum.getCode() == code) {
                return specifyEnum.getDesc();
            }
        }

        return NodeSpecifyEnum.DOCKER.getDesc();
    }
}
