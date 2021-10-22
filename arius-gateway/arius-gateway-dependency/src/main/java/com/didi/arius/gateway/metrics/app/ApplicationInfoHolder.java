package com.didi.arius.gateway.metrics.app;

public class ApplicationInfoHolder {
    private static String MODEL_NAME = "";

    private static String NODE_ID    = "";

    public static String getModelName() {
        return MODEL_NAME;
    }

    public static String getNodeId() {
        return NODE_ID;
    }

    public static void setNodeId(String nodeId) {
        ApplicationInfoHolder.NODE_ID = nodeId;
    }

    public static void setModelName(String modelName) {
        ApplicationInfoHolder.MODEL_NAME = modelName;
    }

}
