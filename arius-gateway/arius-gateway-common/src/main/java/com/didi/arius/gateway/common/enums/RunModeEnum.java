package com.didi.arius.gateway.common.enums;

public enum RunModeEnum {
    READ_WRITE_SHARE(0),
    READ_WRITE_SPLIT(1);
    int runMode;

    RunModeEnum(int runMode) {
        this.runMode = runMode;
    }

    public int getRunMode() {
        return runMode;
    }
}
