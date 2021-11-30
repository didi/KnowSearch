package com.didichuxing.datachannel.arius.admin.remote;

public class InterfaceConstant {

    private InterfaceConstant(){}

    /**
     * es创建多机房索引接口
     */
    public static final String ES_CREATE_PHYSICAL_INDEX     = "/admin/api/v2/op/template/logic/createPhysicalIndexIfAbsent";

    /**
     * agent 指标获取接口
     */
    public static final String AGENT_LOGX_METRICES          = "/agent-manager-gz/api/v1/logx/metrics/agent";

    /**
     * kafkaManager创建topic
     */
    public static final String KAFKA_MANAGER_CREATE          = "/api/v1/third-part/topics";

    /**
     * kafkaManager topic是否存在
     */
    public static final String KAFKA_MANAGER_TOPIC_EXIST     = "/api/v1/third-part/clusters/%s/topics/%s/metadata";

    /**
     * agent logX 工单创建接口
     */
    public static final String AGENT_LOGX_ORDER_CREATE      = "/agent-manager-gz/api/v1/logx/workorder/orders";

    /**
     * agent logX 工单更新接口
     */
    public static final String AGENT_LOGX_ORDER_UPDATE      = "/agent-manager-gz/api/v1/logx/workorder/orders/update";

    /**
     * agent logX 工单状态接口
     */
    public static final String AGENT_LOGX_ORDER_STATUS      = "/agent-manager-gz/api/v1/logx/workorder/orders/status";

    /**
     * agent logX 工单删除接口
     */
    public static final String AGENT_LOGX_ORDER_DELETE      = "/agent-manager-gz/api/v1/logx/workorder/orders/delete";

    /**
     * agent logX 工单更新接口
     */
    public static final String AGENT_UPDATE                 = "/agent-manager-gz/api/v1/logx/workorder/orders/update";

    /**
     *
     */
    public static final String DDMQ_CONSUME_LAG             = "/carrera/api/pub/offset/consumeLag";

    /**
     *
     */
    public static final String DDMQ_MAX_DELAY               = "/carrera/api/pub/offset/maxDelay";

    /**
     *
     */
    public static final String DDMQ_FIND_BY_GROUP           = "/carrera/api/pub/sub/findByGroup";

    /**
     *
     */
    public static final String DQUALITY_COMPELETE           = "/dquality/v1/complete";

    /**
     *
     */
    public static final String DQUALITY_AGENT_CREATECLAN    = "/dquality/v1/complete/createAgentClan";

    /**
     *
     */
    public static final String DQUALITY_AGENT_DISABLECLAN    = "/dquality/v1/complete/disableClan";

    /**
     *  查询消费组消费详情
     */
    public static final String KAFKA_CONSUME_DETAILS = "/api/v1/third-part/{physicalClusterId}/consumers/{consumerGroup}/topics/{topicName}/consume-details";

    /**
     * 创建kafkaappid
     */
    public static final String KAFKA_CREATE_APPID           = "/api/v1/appIds/appId";

    /**
     * 获取应用Topic信息
     */
    public static final String KAFKA_THROTTLE_APPIDS_DETAIL = "/api/v1/third-part/{physicalClusterId}/topics/{topicName}/apps";

    /**
     * 获取Topic实时流量信息
     */
    public static final String KAFKA_STATUS                 = "/api/v1/third-part/{physicalClusterId}/topics/{topicName}/metrics";


    /**
     * Topic实时请求耗时信息
     */
    public static final String KAFKA_REQUEST_STATUS         = "/api/v1/third-part/{physicalClusterId}/topics/{topicName}/request-time";

    /**
     * kafka topic 的健康分接口
     */
    public static final String KAFKA_HEALTH                 = "/api/v1/third-part/{physicalClusterId}/topics/{topicName}/health-score";

    /**
     * logXStream 的 flink 任务提交接口
     */
    public static final String LOGX_STREAM_FLINK_SUBMIT     = "/logx-stream/api/v1/streamsql/submit";


    /***************************************** monitor *******************************************/
    /**
     * odin ns信息
     */
    public static final String ODIN_NS_LIST                 = "/api/v1/ns/children/list";

    /**
     * odin节点信息
     */
    public static final String ODIN_CLUSTER_LIST            = "/api/v1/cluster/su";

    /**
     * odin告警策略
     */
    public static final String ODIN_STRATEGY_ADD_URL            = "/auth/v1/strategy/add";
    public static final String ODIN_STRATEGY_DEL_URL            = "/auth/v1/strategy/del";
    public static final String ODIN_STRATEGY_MODIFY_URL         = "/auth/v1/strategy/modify";
    public static final String ODIN_STRATEGY_QUERY_BY_NS_URL    = "/auth/v1/strategy/query/ns";
    public static final String ODIN_STRATEGY_QUERY_BY_ID_URL    = "/auth/v1/strategy/query/id";
    public static final String ODIN_ALERT_QUERY_BY_NS_AND_PERIOD_URL = "/auth/v1/event/query/ns/period";
    public static final String ODIN_ALERT_QUERY_BY_ID_URL       = "/auth/v1/event/query/id";
    public static final String ODIN_ALL_NOTIFY_GROUP_URL = "/auth/v1/usergroup/group/all";

    /**
     * odin告警屏蔽
     */
    public static final String ODIN_SILENCE_ADD_URL             = "/auth/v1/silence/add";
    public static final String ODIN_SILENCE_RELEASE_URL         = "/auth/v1/silence/release";
    public static final String ODIN_SILENCE_MODIFY_URL          = "/auth/v1/silence/modify";
    public static final String ODIN_SILENCE_QUERY_BY_NS_URL     = "/auth/v1/silence/query/ns";

    /**
     * odin告警指标数据
     */
    public static final String ODIN_COLLECTOR_SINK_DATA_URL     = "/api/v1/collector/push?ns=";
    public static final String ODIN_COLLECTOR_DOWNLOAD_DATA_URL = "/data/query/graph/dashboard/history";

    /**
     * 告警策略
     */
    public static final String N9E_STRATEGY_ADD_URL = "/api/mon/stra";
    public static final String N9E_STRATEGY_DEL_URL = "/api/mon/stra";
    public static final String N9E_STRATEGY_MODIFY_URL = "/api/mon/stra";
    public static final String N9E_STRATEGY_QUERY_BY_NS_URL = "/api/mon/stra";
    public static final String N9E_STRATEGY_QUERY_BY_ID_URL = "/api/mon/stra";
    public static final String N9E_ALERT_QUERY_BY_NS_AND_PERIOD_URL = "/auth/v1/event/query/ns/period";
    public static final String N9E_ALERT_QUERY_BY_ID_URL = "/auth/v1/event/query/id";

    /**
     * 告警屏蔽
     */
    public static final String N9E_SILENCE_ADD_URL = "/auth/v1/silence/add";
    public static final String N9E_SILENCE_RELEASE_URL = "/auth/v1/silence/release";
    public static final String N9E_SILENCE_MODIFY_URL = "/auth/v1/silence/modify";
    public static final String N9E_SILENCE_QUERY_BY_NS_URL = "/auth/v1/silence/query/ns";

    /**
     * 指标数据
     */
    public static final String N9E_COLLECTOR_SINK_DATA_URL = "/push";
    public static final String N9E_COLLECTOR_DOWNLOAD_DATA_URL = "/data/query/graph/dashboard/history";

    /**
     * 告警组
     */
    public static final String N9E_ALL_NOTIFY_GROUP_URL = "/api/rdb/teams/all?limit=10000";


    /**
     * 获取员工名称
     */
    public static final String MAIN_DATA_ACCOUNT_URK            = "/hr-basic/openApi/V2/searchOnJobStaffByKeyWord?keyWord=";

    public static final String MAIN_DATA_ESTIMATED_TEM          = "/ehr/employee/getEstimatedTerminateEmployeeList";

    public static final String MAIN_DATA_STAFFINO_LADP          = "/hr-basic/openApi/V2/getStaffInfoByLdap?ldap=";

    public static final String MAIN_DATA_DEPTINFO               = "/hr-basic/openApi/V2/getDeptInfoByDeptId?deptId=";

    public static final String MAIN_DATA_DEPT_SEARCH            = "/hr-basic/openApi/V2/searchEffectiveDeptByKeyWord?keyWord=";

    /**
     * sso登录接口
     */
    public static final String SSO_LOGIN_URL                        = "/auth/sso/login";

    public static final String SSO_LOGOUT_URL                       = "/auth/ldap/logout";

    public static final String SSO_CHECK_CODE_URL                   = "/auth/sso/api/check_code";

    public static final String SSO_CHECK_TICKET_URL                 = "/auth/sso/api/check_ticket";

    public static final String SSO_GET_USER_BY_TICKET_URL           = "/auth/sso/api/get_user_by_ticket";

    /**
     * 根据模板名称和集群获取，模板所在节点【startTime， endTime】内的平均cpu
     */
    public static final String URL_STATIS_TEMPLATE_NODE_AVG_CPU       = "/template/statis/nodeAvgCpu.do";

    /**
     * 根据模板名称批量获取索引的容量规划统计信息
     */
    public static final String URL_STATIS_TEMPLATE_CAPACITY_METRIC    = "/template/statis/capacity/metric.do";

    /**
     * 根据模板名称批量获取索引的容量规划统计信息
     */
    public static final String URL_STATIS_TEMPLATE_TPS_METRIC         = "/template/statis/v2/tpsMetric.do";

    /**
     * 根据模板id获取月报中模板基本统计信息
     */
    public static final String URL_STATIS_REPORT_TEMPLATE_STATS       = "/template/statis/getTemplateStatsInfo.do";

    /**
     *  获取appID查询信息(top10)
     */
    public static final String URL_REPORT_QUERY_TOP_TEM_INFO          = "/template/statis/getQueryTopNumInfo.do";

    /**
     * 根据模板名称批量获取索引的容量规划统计信息
     */
    public static final String URL_STATIS_TEMPLATE_INDEX_ACCESS_COUNT = "/template/access/indexName/queryRate.do";

    /**
     * 根据逻辑模板id，查询最近days天之内的appid访问列表
     */
    public static final String URL_STATIS_TEMPLATE_ACCESS_APPIDS      = "/template/access/appids.do";

    /**
     * 获取所有索引标签
     */
    public static final String URL_INDEX_LABEL_LIST_ALL_LABEL         = "/template/label/listAllLabel.do";

    /**
     * 获取指定索引模板的所有标签
     */
    public static final String URL_INDEX_LABEL_GET_TEMPLATE_LABEL     = "/template/label/list.do";

    /**
     * 修改指定索引模板的标签
     */
    public static final String URL_INDEX_LABEL_UPDATE_TEMPLATE_LABEL  = "/template/label/update.do";

    /**
     * 获取指定标签id的标签
     */
    public static final String URL_INDEX_LABEL_GET_TEMPLATE_LABELIDS  = "/template/label/listByLabelIds.do";

    /**
     * 获取指定标签id的标签
     */
    public static final String URL_INDEX_LABEL_GET_TEMPLATE_LABELID   = "/template/label/listByLabelId.do";

    /**
     * 获取指定集群rack的磁盘空余空间
     */
    public static final String URL_NODE_STATIS_RACK_GET               = "/node/statis/rack/get.do";

    /**
     * dsl模板审核
     */
    public static final String URL_DSL_TEMPLATE_AUDIT                 = "/dsl/auditDsl.do";

    /**
     * dsl限流值修改
     */
    public static final String URL_DSL_LIMIT_EDIT                     = "/dsl/batchUpdateQueryLimit.do";

    /**
     * 获取逻辑集群的统计信息
     */
    public static final String LOGIC_CLUSTER_STATIS                   = "/logic/cluster/statis.do";

    /**
     * 获取物理集群的统计信息
     */
    public static final String PHY_CLUSTER_STATIS                     = "/phy/cluster/statis.do";

    /**
     * 获取逻辑集群的访问信息
     */
    public static final String LOGIC_CLUSTER_ACCESS                   = "/logic/cluster/access.do";

    /**
     * 获取APP的访问次数
     */
    public static final String URL_APP_ACCESS_COUNT_GET               = "/gateway/join/query/countByAppid.do";

    /**
     * 获取模板价值分统计
     */
    public static final String URL_TEMPLATE_VALUE_LIST                = "/template/value/list.do";

    /**
     * 获取模板价值分统计
     */
    public static final String URL_TEMPLATE_VALUE_GET                 = "/template/value/get.do";

    /**
     * 获取模板价值分统计
     */
    public static final String URL_MAPPING_MANAGE_SHOW_OPTIMIZE_GET   = "/mapping/manage/showOptimize.do";
}
