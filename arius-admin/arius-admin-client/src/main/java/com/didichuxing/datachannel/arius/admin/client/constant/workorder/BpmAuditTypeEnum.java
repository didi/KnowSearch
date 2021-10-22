package com.didichuxing.datachannel.arius.admin.client.constant.workorder;

/**
 * 工单节点动作
 * 不能修改，必须这3个
 *
 * @author d06679
 * @date 2018/10/25
 */
public enum BpmAuditTypeEnum {
                              /**agree*/
                              AGREE("agree"),

                              DISAGREE("disagree"),

                              SUBMIT("submit");

    private String value;

    BpmAuditTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BpmAuditTypeEnum valueFrom(String value) {
        if (value == null) {
            return null;
        }
        for (BpmAuditTypeEnum state : BpmAuditTypeEnum.values()) {
            if (state.getValue().equals(value)) {
                return state;
            }
        }

        return null;
    }
}
