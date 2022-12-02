package com.didi.cloud.fastdump.common.enums;

public enum LuceneVersionEnum {
    LUCENE_5_5_0("5.5.0"),
    LUCENE_7_6_0("7.6.0"),
    LUCENE_8_4_0("8.4.0");

    private String version;

    public String getVersion() {
        return version;
    }

    LuceneVersionEnum(String version) {
        this.version = version;
    }

    public static boolean isExist(String version) {
        if (version == null) { return false;}

        for (LuceneVersionEnum state : LuceneVersionEnum.values()) {
            if (state.getVersion().equals(version)) {
                return true;
            }
        }

        return false;
    }
}
