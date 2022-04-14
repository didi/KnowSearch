package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum;

import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmHostStatusEnum;

public enum ElasticCloudHostStatusEnum {
    PENDING("pending", EcmHostStatusEnum.RUNNING),
    READY("ready", EcmHostStatusEnum.WAITING),
    RUNNING("running", EcmHostStatusEnum.SUCCESS),
    UPDATED("updated", EcmHostStatusEnum.SUCCESS),
    UPDATING("updating", EcmHostStatusEnum.RUNNING),
    CREATING("creating", EcmHostStatusEnum.RUNNING),
    DELETING("deleting", EcmHostStatusEnum.RUNNING),
    FAILED("failed",EcmHostStatusEnum.FAILED),
    UNKNOWN("unknown", EcmHostStatusEnum.UNKNOWN);

    private String value;

    private EcmHostStatusEnum statusEnum;

    ElasticCloudHostStatusEnum(String value, EcmHostStatusEnum statusEnum) {
        this.value = value;
        this.statusEnum = statusEnum;
    }

    public String getValue() {
        return value;
    }

    public EcmHostStatusEnum getStatusEnum() {
        return statusEnum;
    }

    public static EcmHostStatusEnum getEcmHostStatus(String value, String clusterState) {
        /*整个操作失败, 直接返回失败*/
        if (FAILED.getValue().equals(clusterState)) {
            return EcmHostStatusEnum.FAILED;
        }

        for (ElasticCloudHostStatusEnum statusEnum : ElasticCloudHostStatusEnum.values()) {
            if (statusEnum.value.equals(value)) {
                return statusEnum.getStatusEnum();
            }
        }
        return EcmHostStatusEnum.UNKNOWN;
    }
}