package com.didichuxing.datachannel.arius.admin.common.constant;

public enum RunModeEnum {
    READ_WRITE_SHARE(0),
    READ_WRITE_SPLIT(1),
    UNKNOWN(-1);
    int runMode;

    RunModeEnum(int runMode) {
        this.runMode = runMode;
    }

    public int getRunMode() {
        return runMode;
    }

    public static RunModeEnum valueOfMode(Integer runMode) {
        if (runMode == null) {
            return UNKNOWN;
        }
        for (RunModeEnum mode : RunModeEnum.values()) {
            if (mode.getRunMode() == runMode.intValue()) {
                return mode;
            }
        }
        return UNKNOWN;
    }

    public static boolean validate(Integer runMode) {
        return UNKNOWN != valueOfMode(runMode);
    }
}
