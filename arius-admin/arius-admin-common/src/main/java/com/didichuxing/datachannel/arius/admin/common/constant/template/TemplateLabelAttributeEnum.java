package com.didichuxing.datachannel.arius.admin.common.constant.template;

/**
 * @author didi
 */
public enum TemplateLabelAttributeEnum {
    SYS("1", "平台侧"),

    USER("2", "用户侧");


    private String id;

    private String attribute;


    TemplateLabelAttributeEnum(String id, String attribute) {
        this.id = id;
        this.attribute = attribute;
    }

    public String getId() {
        return id;
    }

    public String getAttribute() {
        return attribute;
    }}


