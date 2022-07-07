package com.didichuxing.datachannel.arius.admin.common.constant.template;

/**
 * @author didi
 */
public enum TemplateLabelLevelEnum {
                                    GREEN("1", "green"),

                                    YELLOW("2", "yellow"),

                                    RED("3", "red");

    private final String id;

    private final String level;

    TemplateLabelLevelEnum(String id, String level) {
        this.id = id;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getLevel() {
        return level;
    }


}
