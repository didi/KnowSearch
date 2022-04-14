package com.didi.arius.gateway.core.service.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.FlowStatus;
import com.didi.arius.gateway.common.event.PostResponseEvent;
import com.didi.arius.gateway.common.event.QueryPostResponseEvent;
import com.didi.arius.gateway.common.flowcontrol.*;
import com.didi.arius.gateway.common.metadata.FlowThreshold;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.RateLimitService;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@NoArgsConstructor
public class RateLimitServiceImpl implements RateLimitService, ApplicationListener<PostResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitServiceImpl.class);
    private static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);
    private static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);

    private AtomicLong totalByteIn = new AtomicLong();

    /**
     * 30MB/s
     */
    private static final int APPID_FLOW_LIMIT_IN_UPPER = 30 * 1024 * 1024;
    private static final int APPID_FLOW_LIMIT_IN_LOWER = 15 * 1024 * 1024;
    private static final int APPID_FLOW_LIMIT_OUT_UPPER = 100 * 1024 * 1024;
    private static final int APPID_FLOW_LIMIT_OUT_LOWER = 80 * 1024 * 1024;
    private static final int APPID_FLOW_LIMIT_OPS_UPPER = 10000;
    private static final int APPID_FLOW_LIMIT_OPS_LOWER = 5000;

    @Value("${arius.gateway.flowSchedulePeriod}")
    private int flowSchedulePeriod;

    private ConcurrentMap<Integer, FlowController> flowControllerMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, FlowLimit> flowLimitMap = new ConcurrentHashMap<>();

    @Autowired
    private QueryConfig queryConfig;

    @Autowired
    private ThreadPool threadPool;

    @PostConstruct
    public void init(){
        cacheTotalAreaFlow();
        threadPool.submitScheduleAtFixTask(this::calFlowLimit, flowSchedulePeriod, flowSchedulePeriod);
    }

    @Override
    public void addByteIn(long bytes) {
        long currentByteIn = totalByteIn.get();
        if (currentByteIn >= queryConfig.getMaxByteIn()) {
            bootLogger.error("totalByteIn overflow, currentByteIn is {}", currentByteIn);
        }

        totalByteIn.addAndGet(bytes);
    }

    @Override
    public void removeByteIn(long bytes) {
        totalByteIn.addAndGet(-bytes);
    }

    @Override
    public boolean isTrafficDataOverflow(int appid, String searchId) {
        String areaId = FlowController.formAreaId(appid, searchId);
        FlowLimit flowLimit = flowLimitMap.get(areaId);
        if (flowLimit == null) {
            return false;
        }

        boolean ret = flowLimit.isOverflow();
        if (ret) {
            statLogger.info(QueryConsts.DLFLAG_PREFIX + "overflow||appid={}||searchId={}||threshold={}", appid, searchId, flowLimit.getThreshold());
        }

        return ret;
    }

    @Override
    public void addUp(int appid, String searchId, int in, int out) {
        FlowController flowController = getFlowController(appid);
        boolean isRelaxedIn = flowController.addUpIn(searchId, in);
        boolean isRelaxedOut = flowController.addUpOut(searchId, out);
        if (!isRelaxedIn || !isRelaxedOut) {
            String areaId = FlowController.formAreaId(appid, searchId);
            initFlowLimit(areaId);
        }
    }

    @Override
    public void resetAppAreaFlow(int appid, FlowThreshold flowThreshold) {
        String areaId = FlowController.formAreaId(appid, QueryConsts.TOTAL_SEARCH_ID);
        AreaFlow areaFlow = AreaFlowCache.getInstance().getAreaFlow(areaId);
        if (areaFlow == null) {
            cacheAppAreaFlow(appid, flowThreshold);
        } else {
            Flow in = areaFlow.getIn();
            in.setLowerBound(flowThreshold.getInLower());
            in.setUpperBound(flowThreshold.getInUpper());

            Flow out = areaFlow.getOut();
            out.setLowerBound(flowThreshold.getOutLower());
            out.setUpperBound(flowThreshold.getOutUpper());

            Flow ops = areaFlow.getOps();
            ops.setLowerBound(flowThreshold.getOpsLower());
            ops.setUpperBound(flowThreshold.getOpsUpper());
        }
    }

    @Override
    public Map<Integer, FlowController> getFlowControllerMap() {
        return flowControllerMap;
    }

    @Override
    public ConcurrentMap<String, FlowLimit> getFlowLimitMap() {
        return flowLimitMap;
    }

    @Override
    public int getFlowSchedulePeriod() {
        return flowSchedulePeriod;
    }

    @Override
    public void onApplicationEvent(PostResponseEvent postResponseEvent) {

        if(postResponseEvent instanceof QueryPostResponseEvent){
            QueryPostResponseEvent queryPostResponseEvent = (QueryPostResponseEvent)postResponseEvent;
            QueryContext queryContext = queryPostResponseEvent.getQueryContext();
            if(null != queryContext){
                removeByteIn(queryContext.getPostBody().length());
            }
        }
    }

    /************************************************************** private method **************************************************************/
    private void cacheTotalAreaFlow() {
        Flow in = new Flow();
        in.setLowerBound(APPID_FLOW_LIMIT_IN_LOWER * 10);
        in.setUpperBound(APPID_FLOW_LIMIT_IN_UPPER * 10);

        Flow out = new Flow();
        out.setLowerBound(APPID_FLOW_LIMIT_OUT_LOWER * 10);
        out.setUpperBound(APPID_FLOW_LIMIT_OUT_UPPER * 10);

        Flow ops = new Flow();
        ops.setLowerBound(APPID_FLOW_LIMIT_OPS_LOWER * 10);
        ops.setUpperBound(APPID_FLOW_LIMIT_OPS_UPPER * 10);

        AreaFlow areaFlow = new AreaFlow();
        areaFlow.setIn(in);
        areaFlow.setOut(out);
        areaFlow.setOps(ops);
        areaFlow.setStatus(FlowStatus.DOWN);

        String areaId = FlowController.formAreaId(QueryConsts.TOTAL_APPID_ID, QueryConsts.TOTAL_SEARCH_ID);
        AreaFlowCache.getInstance().setAreaFlow(areaId, areaFlow);
    }

    private FlowController getFlowController(int appid) {
        if (!flowControllerMap.containsKey(appid)) {
            synchronized (flowControllerMap) {
                flowControllerMap.computeIfAbsent(appid, i -> new FlowController(appid, flowSchedulePeriod));
            }
        }

        return flowControllerMap.get(appid);
    }

    private FlowLimit initFlowLimit(String areaId) {
        FlowLimit flowLimit = flowLimitMap.get(areaId);
        if (flowLimit == null) {
            flowLimit = new FlowLimit(areaId);
            flowLimit = flowLimitMap.putIfAbsent(areaId, flowLimit);
        }

        return flowLimit;
    }

    private void cacheAppAreaFlow(int appid, FlowThreshold flowThreshold) {
        Flow in = new Flow();
        in.setLowerBound(APPID_FLOW_LIMIT_IN_LOWER);
        in.setUpperBound(APPID_FLOW_LIMIT_IN_UPPER);

        Flow out = new Flow();
        out.setLowerBound(APPID_FLOW_LIMIT_OUT_LOWER);
        out.setUpperBound(APPID_FLOW_LIMIT_OUT_UPPER);

        Flow ops = new Flow();
        ops.setLowerBound(flowThreshold.getOpsLower());
        ops.setUpperBound(flowThreshold.getOpsUpper());

        AreaFlow areaFlow = new AreaFlow();
        areaFlow.setIn(in);
        areaFlow.setOut(out);
        areaFlow.setOps(ops);
        areaFlow.setStatus(FlowStatus.DOWN);

        String areaId = FlowController.formAreaId(appid, QueryConsts.TOTAL_SEARCH_ID);
        AreaFlowCache.getInstance().setAreaFlow(areaId, areaFlow);
    }

    private void calFlowLimit() {
        for (Map.Entry<Integer, FlowController> entry : flowControllerMap.entrySet()) {
            FlowController flowController = entry.getValue();

            flowController.backgroundCalFlows();
        }

        for (Map.Entry<String, FlowLimit> entry : flowLimitMap.entrySet()) {
            String areaId = entry.getKey();
            FlowLimit flowLimit = entry.getValue();
            AreaFlow areaFlow = AreaFlowCache.getInstance().getAreaFlow(areaId);

            flowLimit.limitLevelTouch(areaFlow.getStatus());
        }
    }
}
