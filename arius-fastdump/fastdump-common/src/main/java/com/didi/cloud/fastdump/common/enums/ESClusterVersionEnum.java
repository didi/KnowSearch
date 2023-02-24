package com.didi.cloud.fastdump.common.enums;

public enum ESClusterVersionEnum {
                                  ES_2_3_3("2.3.3"),

                                  ES_6_5_1("6.5.1"),

                                  ES_6_6_1("6.6.1"),

                                  ES_6_6_6("6.6.6"),

                                  ES_6_7_0("6.7.0"),

                                  ES_7_0_0("7.0.0"),

                                  ES_7_6_0("7.6.0"),

                                  ES_8_4_0("8.4.0");

    private String version;

    public String getVersion() {
        return version;
    }

    ESClusterVersionEnum(String version) {
        this.version = version;
    }

    public static boolean isExist(String version) {
        if (version == null) { return false;}

        for (ESClusterVersionEnum state : ESClusterVersionEnum.values()) {
            if (state.getVersion().equals(version)) {
                return true;
            }
        }

        return false;
    }
}
