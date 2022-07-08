package com.didichuxing.datachannel.arius.admin.common.constant;

public enum ESClusterVersionEnum {

    /**
     * 6.6.1.1001版本
     */
    ES_2_3_3_100("2.3.3.100"),

    ES_6_5_1_100("6.5.1.500"),

    ES_6_6_1_700("6.6.1.700"),

    ES_6_6_1_704("6.6.1.704"),

    ES_6_6_1_800("6.6.1.800"),

    ES_6_6_1_902("6.6.1.902"),

    ES_6_6_6_800("6.6.6.800"),

    ES_6_6_6_900("6.6.6.900"),

    ES_6_7_0_1002("6.7.0.1002"),

    ES_6_7_0_1003("6.7.0.1003"),

    ES_6_7_0_1004("6.7.0.1004"),

    ES_6_7_0_1005("6.7.0.1005"),

    ES_7_6_0_1100("7.6.0.1100"),

    ES_7_6_0_1108("7.6.0.1108"),

    ES_7_6_0_1201("7.6.0.1201"),

    ES_7_6_0_1202("7.6.0.1202");

    private String version;

    public String getVersion(){
        return version;
    }

    ESClusterVersionEnum(String version) {
        this.version   = version;
    }
}
