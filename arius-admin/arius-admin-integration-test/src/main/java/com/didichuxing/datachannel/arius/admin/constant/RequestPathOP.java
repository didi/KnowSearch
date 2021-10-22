package com.didichuxing.datachannel.arius.admin.constant;

public class RequestPathOP {
    public static final String OP = "/v2/op";

    public static final String CLUSTER_LIST = OP + "/cluster/list";
    public static final String CLUSTER_GET = OP + "/cluster/get?clusterId=%d";

    public static final String TEMPLATE_PHYSICAL_LIST = OP + "/template/physical/list";

    public static final String PHY_CLUSTER_REGION_INFO = OP + "/phy/cluster/%d/regioninfo";

    public static final String APP_ADD = OP + "/app/add";
    public static final String APP_LIST = OP + "/app/list";

}
