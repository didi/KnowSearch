package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum;

public enum EcmActionEnum {
//    START("start"),
//    SCALE("scale"),
//    UPGRADE("upgrade"),
//    RESTART("restart"),
    REMOVE("remove"),
//    MODIFY("modify"),

    PAUSE("pause"),
    CONTINUE("continue"),
    REDO_FAILED("redo"),
    SKIP_FAILED("ignore"),

    ;
    private String action;

    EcmActionEnum(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}