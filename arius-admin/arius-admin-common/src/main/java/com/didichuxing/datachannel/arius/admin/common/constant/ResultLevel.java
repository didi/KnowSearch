package com.didichuxing.datachannel.arius.admin.common.constant;

public enum ResultLevel {
    ERROR(1,    "Error"),
    CRITICAL(2, "Critical"),
    MAJOR(3,    "Major"),
    NORMAL(4,   "Normal"),
    MINOR(5,    "Minor"),
    FINE(6,     "Fine");

    private int    code;
    private String name;

    private ResultLevel(int code, String name){
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ResultLevel getResultlevel(String[] levelConfig, double badPercent) {
        ResultLevel result = null;

        if (null == levelConfig || levelConfig.length < 5) {
            levelConfig = new String[] { "5", "10", "15", "20", "25" };
        }

        if (Double.parseDouble(levelConfig[0]) / 100 >= badPercent) {
            result = FINE;
        } else if (Double.parseDouble(levelConfig[0]) / 100 < badPercent
                && badPercent <= Double.parseDouble(levelConfig[1]) / 100) {
            result = MINOR;
        } else if (Double.parseDouble(levelConfig[1]) / 100 < badPercent
                && badPercent <= Double.parseDouble(levelConfig[2]) / 100) {
            result = NORMAL;
        } else if (Double.parseDouble(levelConfig[2]) / 100 < badPercent
                && badPercent <= Double.parseDouble(levelConfig[3]) / 100) {
            result = MAJOR;
        } else if (Double.parseDouble(levelConfig[3]) / 100 < badPercent
                && badPercent <= Double.parseDouble(levelConfig[4]) / 100) {
            result = CRITICAL;
        } else if (badPercent > Double.parseDouble(levelConfig[4]) / 100) {
            result = ERROR;
        }

        return result;
    }
}
