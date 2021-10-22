package com.didichuxing.datachannel.arius.admin.remote.monitor.odin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.common.OdinData;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Alert;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Metric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MetricSinkPoint;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.NotifyGroup;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Silence;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Strategy;
import com.didichuxing.datachannel.arius.admin.common.component.RestTool;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinAlert;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinCluster;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinSilence;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinStrategy;
import com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean.OdinTreeNode;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_ALERT_QUERY_BY_ID_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_ALERT_QUERY_BY_NS_AND_PERIOD_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_ALL_NOTIFY_GROUP_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_CLUSTER_LIST;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_COLLECTOR_DOWNLOAD_DATA_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_COLLECTOR_SINK_DATA_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_NS_LIST;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_SILENCE_ADD_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_SILENCE_MODIFY_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_SILENCE_QUERY_BY_NS_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_SILENCE_RELEASE_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_STRATEGY_ADD_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_STRATEGY_DEL_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_STRATEGY_MODIFY_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_STRATEGY_QUERY_BY_ID_URL;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.ODIN_STRATEGY_QUERY_BY_NS_URL;

/**
 * @author d06679
 * @date 2019/5/24
 */
@Service("odinRemoteServiceImpl")
@ConditionalOnProperty(value = "monitor.odin.enable", havingValue = "true")
public class OdinRemoteServiceImpl implements RemoteMonitorService {

    private final ILog                                                               LOGGER    = LogFactory
        .getLog(OdinRemoteServiceImpl.class);

    private static final String                                                      DIDILABEL = ".didi.com";

    @Value("${monitor.odin.monitor.url}")
    private String                                                                   odinMonitorUrl;

    @Value("${monitor.odin.collector.url}")
    private String                                                                   odinCollectorUrl;

    @Value("${monitor.odin.namespace}")
    private String                                                                   odinMonitorNamespace;

    @Value("${monitor.odin.securename}")
    private String                                                                   odinSecureName;

    @Value("${monitor.odin.securekey}")
    private String                                                                   odinSecureKey;

    @Value("${cloud.basic.treeServer}")
    private String                                                                   treeServer;

    @Value("${cert.calleeName}")
    private String                                                                   calleeName;

    @Value("${cert.keyName}")
    private String                                                                   keyName;

    @Autowired
    private RestTool                                                                 restTool;

    private Cache<String, Map<String /* machineRoom */, Set<String> /* clusters */>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .concurrencyLevel(10)
            .build();

    /**
     * 向Odin指定ns发送数据
     *
     * @param odinDatas 需要发送的数据
     * @return true/false
     */
    @Override
    public boolean sendData(Collection<OdinData> odinDatas) {
        String collectUrl = odinCollectorUrl + ODIN_COLLECTOR_SINK_DATA_URL + "collect." + odinMonitorNamespace;
        Map resp = restTool.postObjectWithJsonContent(collectUrl, odinDatas, Map.class);

        boolean succ = false;
        if (resp.containsKey("code")) {
            Object code = resp.get("code");
            if (Double.valueOf(String.valueOf(code)).equals(0.0)) {
                succ = true;
            }
        }

        return succ;
    }

    @Override
    public List<OdinCluster> getOdinChildren(String odinService) {
        if (!odinService.endsWith(DIDILABEL)) {
            odinService += DIDILABEL;
        }
        String odinServiceUrl = treeServer + ODIN_NS_LIST + "?ns=" + odinService;
        String odinchildrenStr = restTool.getForString(odinServiceUrl, null);

        return JSON.parseArray(odinchildrenStr, OdinCluster.class);
    }

    @Override
    public List<OdinCluster> getOdinCluster(Set<String> odinClusters) {
        String odinServiceUnio = StringUtils.arrayToDelimitedString(odinClusters.toArray(), ",");
        String odinServiceUrl = treeServer + ODIN_CLUSTER_LIST + "?sus=" + odinServiceUnio;
        String odinchildrenStr = restTool.getForString(odinServiceUrl, null);

        return JSON.parseArray(odinchildrenStr, OdinCluster.class);
    }

    /**
     * 通过source获取相关机房
     *
     * @param odinServicePath
     * @return key为机房信息，value为所有odin节点信息
     */
    @Override
    public Map<String /* machineRoom */, Set<String> /* clusters */> getMachineRoomsByOdin(String odinServicePath, List<String> clusterInfoPOS) {
        Map<String, Set<String>> machineRoomAndLeaf = cache.getIfPresent(odinServicePath);
        if (machineRoomAndLeaf != null) {
            return machineRoomAndLeaf;
        }

        Map<String, Set<String>> clusters = getMachineRoomsRealTimeByOdin(odinServicePath, clusterInfoPOS);
        cache.put(odinServicePath, clusters);
        return clusters;
    }

    @Override
    public Map<String /* machineRoom */, Set<String> /* clusters */> getMachineRoomsRealTimeByOdin(String odinServicePath, List<String> clusterInfoPOS) {
        Map<String, Set<String>> clusters = new HashMap<>();

        List<OdinCluster> odinClusterBeans = getOdinChildren(odinServicePath);
        if(odinClusterBeans.isEmpty()) {
            return clusters;
        }

        Map<String, String> odinServices = new HashMap();
        for (OdinCluster odinClusterBean : odinClusterBeans) {
            odinServices.put(odinClusterBean.getSu(), odinClusterBean.getName());
        }

        List<OdinCluster> odinClusterInfos = getOdinCluster(odinServices.keySet());
        Map<String, String> odinClusterBeanMap = new HashMap();
        for (OdinCluster odinClusterBean : odinClusterInfos) {
            String[] clustersByOdin = odinClusterBean.getCluster();
            //增加按域过滤
            for (String clusterName : clustersByOdin) {
                if (clusterInfoPOS.contains(clusterName)) {
                    odinClusterBeanMap.put(odinClusterBean.getSu(), clusterName);
                } else {
                    LOGGER.warn("clusterName:{} not contains in clusterInfoPOS:{} for den:{}", clusterName, clusterInfoPOS, EnvUtil.getDC().getCode());
                }
            }
        }

        for (Map.Entry<String, String> entry : odinClusterBeanMap.entrySet()) {
            String cluster = entry.getValue();
            if (clusters.containsKey(cluster)) {
                clusters.get(cluster).add(odinServices.get(entry.getKey()));
            } else {
                Set<String> nameSet = new HashSet<>();
                nameSet.add(odinServices.get(entry.getKey()));
                clusters.put(cluster, nameSet);
            }
        }

        return clusters;
    }

    @Override
    public Integer createStrategy(Strategy strategy) {
        return createOrModifyOdinMonitorStrategy(odinMonitorUrl + ODIN_STRATEGY_ADD_URL, strategy);
    }

    @Override
    public Integer modifyStrategy(Strategy strategy) {
        return createOrModifyOdinMonitorStrategy(odinMonitorUrl + ODIN_STRATEGY_MODIFY_URL, strategy);
    }

    @Override
    public Boolean deleteStrategyById(Long strategyId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("ns", odinMonitorNamespace);
        params.put("ids", Arrays.asList(strategyId));

        Result ret = restTool.postObjectWithJsonContentAndHeader(odinMonitorUrl + ODIN_STRATEGY_DEL_URL,
                buildHeader(), params,
                new TypeReference<Result>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return false;
        }
        return true;
    }

    @Override
    public List<Strategy> getStrategies() {
        String url = odinMonitorUrl + ODIN_STRATEGY_QUERY_BY_NS_URL + "?ns=" + odinMonitorNamespace;

        Result<List<OdinStrategy>> ret = restTool.getForObject(url, buildHeader(),
                new TypeReference<Result<List<OdinStrategy>>>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return null;
        }
        return OdinConverter.convert2StrategyList(ret.getData());
    }

    @Override
    public Strategy getStrategyById(Long strategyId) {
        String url = odinMonitorUrl + ODIN_STRATEGY_QUERY_BY_ID_URL + "?id=" + strategyId;

        Result<OdinStrategy> ret = restTool.getForObject(url, buildHeader(),
                new TypeReference<Result<OdinStrategy>>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return null;
        }
        return OdinConverter.convert2Strategy(ret.getData());
    }

    @Override
    public List<Alert> getAlerts(Long strategyId, Long startTime, Long endTime) {
        Map<String, String> params = new HashMap<>(1);
        params.put("ns", odinMonitorNamespace);
        params.put("s", String.valueOf(startTime));
        params.put("e", String.valueOf(endTime));

        String url = RestTool.getQueryString(odinMonitorUrl + ODIN_ALERT_QUERY_BY_NS_AND_PERIOD_URL, params);
        Result<List<OdinAlert>> ret = restTool.getForObject(url, buildHeader(),
                new TypeReference<Result<List<OdinAlert>>>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return null;
        }

        return OdinConverter.convert2AlertList(ret.getData()).stream()
                .filter(elem -> elem.getStrategyId().equals(strategyId))
                .collect( Collectors.toList());
    }

    @Override
    public Alert getAlertById(Long alertId) {
        Map<String, String> params = new HashMap<>(1);
        params.put("id", String.valueOf(alertId));

        String url = RestTool.getQueryString(odinMonitorUrl + ODIN_ALERT_QUERY_BY_ID_URL, params);

        Result<OdinAlert> ret = restTool.getForObject(url, buildHeader(),
                new TypeReference<Result<OdinAlert>>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return null;
        }
        return OdinConverter.convert2Alert(ret.getData());
    }

    @Override
    public Boolean createSilence(Silence silence) {
        return createOrModifySilence(odinMonitorUrl + ODIN_SILENCE_ADD_URL, silence);
    }

    @Override
    public Boolean modifySilence(Silence silence) {
        return createOrModifySilence(odinMonitorUrl + ODIN_SILENCE_MODIFY_URL, silence);
    }

    @Override
    public Boolean releaseSilence(Long silenceId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("ns", odinMonitorNamespace);
        params.put("ids", Arrays.asList(silenceId));

        String url = odinMonitorUrl + ODIN_SILENCE_RELEASE_URL;
        Result ret = restTool.postObjectWithJsonContentAndHeader(url, buildHeader(), params,
                new TypeReference<Result>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return false;
        }
        return true;
    }

    @Override
    public List<Silence> getSilences(Long strategyId) {
        List<OdinSilence> odinSilenceList = getSilence();
        if (odinSilenceList == null) {
            return null;
        }

        String strategyIdStr = String.valueOf(strategyId);
        return OdinConverter.convert2SilenceList(odinSilenceList
                .stream()
                .filter(elem -> strategyIdStr.equals(elem.getSids()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public Silence getSilenceById(Long silenceId) {
        List<OdinSilence> odinSilenceList = getSilence();
        if (odinSilenceList == null) {
            return null;
        }

        for (OdinSilence odinSilence : odinSilenceList) {
            if (odinSilence.getId().equals(silenceId.intValue())) {
                return OdinConverter.convert2Silence(odinSilence);
            }
        }
        return null;
    }

    @Override
    public List<NotifyGroup> getNotifyGroups() {
        String url = RestTool.getQueryString(odinMonitorUrl + ODIN_ALL_NOTIFY_GROUP_URL, null);

        Result<List<NotifyGroup>> ret = restTool.getForObject(url, buildHeader(),
                new TypeReference<Result<List<NotifyGroup>>>() {}.getType());
        if (null == ret || ret.failed()) {
            return null;
        }
        return ret.getData();
    }

    /**
     * 指标上报
     */
    @Override
    public Boolean sinkMetrics(List<MetricSinkPoint> pointList) {
        String url = odinMonitorUrl + ODIN_COLLECTOR_SINK_DATA_URL + "collect." + odinMonitorNamespace;

        Result ret = restTool.postObjectWithJsonContentAndHeader(url, null, pointList,
                new TypeReference<Result>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return false;
        }
        return true;
    }

    @Override
    public Metric getMetrics(String metric, Long startTime, Long endTime, Integer step, Properties tags) {
        Map<String, Object> endpointCounter = new HashMap<>();
        endpointCounter.put("ns", "collect." + odinMonitorNamespace);
        endpointCounter.put("start", startTime / 1000);
        endpointCounter.put("end", endTime / 1000);
        endpointCounter.put("metric", metric);
        endpointCounter.put("tags", OdinConverter.convert2TagStr(tags));
        endpointCounter.put("step", step);

        Map<String, Object> params = new HashMap<>();
        params.put("endpoint_counters", Arrays.asList(endpointCounter));

        String url = odinMonitorUrl + ODIN_COLLECTOR_DOWNLOAD_DATA_URL;

        Result<List<Metric>> ret = restTool.postObjectWithJsonContentAndHeader(url, buildHeader(), params,
                new TypeReference<Result<List<Metric>>>() {
                }.getType());

        if (null == ret || ret.failed() || CollectionUtils.isEmpty(ret.getData())) {
            return null;
        }
        return ret.getData().get(0);
    }

    @Override
    public OdinTreeNode getOdinTreeNode(String username) {
        String url = treeServer+"/api/v1/user/tree?username="+username;
        String result = BaseHttpUtil.get(url,null);
        if (result == ""){
            return new OdinTreeNode();
        }
        OdinTreeNode odinTreeNode = JSONObject.parseObject(result, OdinTreeNode.class);
        if ( odinTreeNode == null ) {
            return new OdinTreeNode();
        }
        return odinTreeNode;
    }

    @Override
    public Result createTreeNode(String ns, String category, String usn, int level) {
        String url = treeServer+"/auth/v1/ns/create";
        calleeName = calleeName.trim();
        keyName = keyName.trim();

        Map<String, String> headers = buildAuthorization(calleeName, keyName);
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", ns);
        params.put("category", category);
        params.put("usn", usn);
        params.put("serviceLevel", level);

        String result = BaseHttpUtil.post(url, params,  headers, "UTF-8", "UTF-8");
        LOGGER.info("class=OdinRemoteServiceImpl||method=createTreeNode||result={}||treeNode={}",
                result,ns);
        if(result.equals("")){
            return Result.buildSucc();
        }else{
            LOGGER.error("class=ElasticCloudRemoteServiceImpl||method=createTreeNode||errMsg={}||treeNode={}",
                    result,ns);
            return Result.buildFail(result);
        }
    }

    @Override
    public Result deleteTreeNode(String ns, String category, String usn, int level) {
        String url = treeServer+"/auth/v1/ns/delete";
        calleeName = calleeName.trim();
        keyName = keyName.trim();

        Map<String, String> headers = buildAuthorization(calleeName, keyName);
        Map<String, Object> params = Maps.newHashMap();
        params.put("ns", ns);
        params.put("category", category);
        params.put("usn", usn);
        params.put("serviceLevel", level);

        String result = BaseHttpUtil.post(url, params,  headers, "UTF-8", "UTF-8");
        if(result.equals("")){
            return Result.buildSucc();
        }else {
            return Result.buildFail(result);
        }
    }

    /******************************************** private methods ********************************************/
    private List<OdinSilence> getSilence() {
        Map<String, String> params = new HashMap<>(2);
        params.put("ns", odinMonitorNamespace);
        params.put("effective", String.valueOf(true));

        String url = RestTool.getQueryString(odinMonitorUrl + ODIN_SILENCE_QUERY_BY_NS_URL, params);

        Result<List<OdinSilence>> ret = restTool.getForObject(url, buildHeader(),
                new TypeReference<Result<List<OdinSilence>>>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return null;
        }
        return ret.getData();
    }

    private Boolean createOrModifySilence(String url, Silence silence) {
        OdinSilence odinSilence = OdinConverter.convert2OdinSilenceCreation(silence, odinMonitorNamespace);

        Result ret = restTool.postObjectWithJsonContentAndHeader(url, buildHeader(), odinSilence,
                new TypeReference<Result>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return false;
        }
        return true;
    }

    private Integer createOrModifyOdinMonitorStrategy(String url, Strategy strategy) {
        OdinStrategy odinStrategy = OdinConverter.convert2OdinStrategy(strategy, odinMonitorNamespace);

        Result<Integer> ret = restTool.postObjectWithJsonContentAndHeader(url, buildHeader(), odinStrategy,
                new TypeReference<Result<Integer>>() {
                }.getType());

        if (null == ret || ret.failed()) {
            return null;
        }
        return ret.getData();
    }

    private Map<String, String> buildHeader() {
        Map<String, String> header = new HashMap<>(1);
        header.put("Content-Type", "application/json");
        Map<String, String> alarmConfig = OdinCert.buildAuthorization(odinSecureName, odinSecureKey);
        header.putAll(alarmConfig);
        return header;
    }

    private static final long TOKEN_TIME_SPAN = 300; // token 有效时间 300 s

    // 生成auth头
    private Map<String, String> buildAuthorization(String sys, String skey) { // sys为系统名, skey为sys的秘钥
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", genAuth(sys, skey));
        return headers;
    }

    private  String genAuth(String callerName, String skey) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cert caller=").append(callerName).append(",token=").append(genToken(callerName, skey));
        return sb.toString();
    }

    private String genToken(String callerName, String skey) {
        long currentTs = System.currentTimeMillis() / 1000L;
        long tknTs = currentTs - currentTs % TOKEN_TIME_SPAN;
        return DigestUtils.md5Hex("" + tknTs + "." + callerName + "." + skey);
    }
}
