package com.didichuxing.datachannel.arius.admin.common.constant.template;

/**
 * @author didi
 */
public enum TemplateLabelSourceEnum {

    ADMIN("1", "admin"),

    AMS("2", "ams"),

    PLATFORM_GOVERN("3", "平台治理");


    private final String id;

    private final String source;


    TemplateLabelSourceEnum(String id, String source) {
        this.id = id;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }}
