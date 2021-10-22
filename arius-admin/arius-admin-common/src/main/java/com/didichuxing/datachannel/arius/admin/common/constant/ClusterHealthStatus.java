package com.didichuxing.datachannel.arius.admin.common.constant;

public enum ClusterHealthStatus {
    GREEN((byte) 0),
    YELLOW((byte) 1),
    RED((byte) 2);

    private byte value;

    ClusterHealthStatus(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }

    public static ClusterHealthStatus fromValue(byte value) {
        switch (value) {
            case 0:
                return GREEN;
            case 1:
                return YELLOW;
            case 2:
                return RED;
            default:
                throw new IllegalArgumentException("No cluster health status for value [" + value + "]");
        }
    }

    public static ClusterHealthStatus fromString(String status) {
        if (status.equalsIgnoreCase("green")) {
            return GREEN;
        } else if (status.equalsIgnoreCase("yellow")) {
            return YELLOW;
        } else if (status.equalsIgnoreCase("red")) {
            return RED;
        } else {
            throw new IllegalArgumentException("unknown cluster health status [" + status + "]");
        }
    }
}
