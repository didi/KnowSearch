package com.didichuxing.datachannel.arius.admin.core.notify;

/**
 * 通知渠道
 *
 * @author didi
 */
public enum NotifyChannelEnum {

                               /**
                                * email
                                */
                               EMAIL("email"),

                               /**
                                * sms
                                */
                               SMS("sms"),

                               /**
                                * voice
                                */
                               VOICE("voice");

    private String name;

    NotifyChannelEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static NotifyChannelEnum valueByDesc(String desc) {
        for (NotifyChannelEnum channel : NotifyChannelEnum.values()) {
            if (channel.getName().equals(desc)) {
                return channel;
            }
        }

        return EMAIL;
    }
}
