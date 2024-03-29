package com.didichuxing.datachannel.arius.admin.common.constant;

public enum PageSearchHandleTypeEnum {
        UNKNOWN("unknown"),
        TEMPLATE_LOGIC("templateLogicPageSearch"),
        CLUSTER_LOGIC("clusterLogicPageSearch"),
        DSL_TEMPLATE("dslTemplatePageSearch"),
        CLUSTER_PHY("clusterPhyPageSearch"),
        INDEX("indexPageSearch"),
        TEMPLATE_SRV("templateSrvPageSearch"),

        QUICK_COMMAND_INDEX("quickCommandIndicesDistributionPageSearch"),
        QUICK_COMMAND_SHARD("quickCommandShardsDistributionPageSearch"),
        OPERATE_RECORD("operateRecordPageSearch"),
        GATEWAY_JOIN("gatewayJoinPageSearch"),
        TASK("taskPageSearch");
       private String pageSearchType;

        public String getPageSearchType(){
            return pageSearchType;
        }

        PageSearchHandleTypeEnum(String pageSearchType) {
            this.pageSearchType   = pageSearchType;
        }
    }