package com.didi.arius.gateway.core.es.tcp;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.FlowLimitException;
import com.didi.arius.gateway.common.exception.ServerBusyException;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.es.http.ESBase;
import com.didi.arius.gateway.core.service.*;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.log.LogGather;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
* @author weizijun
* @date：2016年9月18日
* 
*/
@Component("actionHandler")
public abstract class ActionHandler extends ESBase {
	protected static final ILog logger = LogFactory.getLog(ActionHandler.class);
	protected static final ILog statLogger = LogFactory.getLog(QueryConsts.STAT_LOGGER);
	protected static final ILog traceLogger = LogFactory.getLog(QueryConsts.TRACE_LOGGER);
	
	@Autowired
	protected ActionController controller;
	
	@Autowired
	protected ESTcpClientService esTcpClientService;
	
    @Autowired
    protected RateLimitService rateLimitService;

	@Autowired
	protected QueryConfig queryConfig;

	@Autowired
	protected RequestStatsService requestStatsService;
	@Autowired
	protected MetricsService metricsService;
	
	public void handleRequest(ActionContext actionContext) throws IOException {
		try {
			if (!actionContext.getSemaphore().tryAcquire(10, TimeUnit.MILLISECONDS)) {
				throw new ServerBusyException("too many block queries, please wait and retry");
			}
			
			checkToken(actionContext);
			
			checkFlowLimit(actionContext);
			
			preRequest(actionContext);
			
	        handleInterRequest(actionContext, 0);
	        
	        postRequest(actionContext);
		} catch (Exception e) {
			if (!(e instanceof ServerBusyException)) {
				actionContext.getSemaphore().release();
			}

			preException(actionContext, e);
			
			TransportChannel channel = actionContext.getChannel();
			channel.sendResponse(e);

			requestStatsService.removeActionContext(actionContext.getRequestId());
		}
	}

	protected void checkToken(ActionContext actionContext) {
		appService.checkToken(actionContext);
		String encode = Base64.getEncoder().encodeToString(String.format("%s", "user_" + actionContext.getAppid() + ":" + actionContext.getAppDetail().getVerifyCode()).getBytes(StandardCharsets.UTF_8));
		actionContext.getRequest().putHeader("Authorization", "Basic " + encode);
	}
	
	public abstract void handleInterRequest(ActionContext actionContext, int retryTimes) throws Exception;

	protected abstract  Class<? extends TransportRequest> getRequestClass();
	
	public abstract String name();
	
	public TransportRequest parseRequest(TransportAddress remoteAddress, StreamInput buffer) throws InstantiationException, IllegalAccessException, IOException {
        Class<? extends TransportRequest> cls = getRequestClass();

        final TransportRequest request = cls.newInstance();
        request.remoteAddress(remoteAddress);
        request.readFrom(buffer);
        
        return request;
	}
	
	protected void preRequest(ActionContext actionContext) {
		statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_request||appid={}||requestId={}||action={}||group={}||clusterId={}||clusterName={}||user={}||remoteAddr={}||requestLen={}",
				actionContext.getAppid(), actionContext.getRequestId(), actionContext.getActionName(), QueryConsts.GATEWAY_GROUP, actionContext.getClusterId(), actionContext.getCluster(), actionContext.getUser(),
				actionContext.getRemoteAddr(), actionContext.getRequestLength());
		actionContext.setRequestTime(System.currentTimeMillis());
		

		traceLogger.info("_com_request_in||traceid={}||spanid={}||type=tcp||appid={}||requestId={}||action={}||remoteAddr={}||requestLen={}",
				actionContext.getTraceid(), actionContext.getSpanid(), actionContext.getAppid(), actionContext.getRequestId(), actionContext.getActionName(), actionContext.getRemoteAddr(), actionContext.getRequestLength());
	}
	
	protected void postRequest(ActionContext actionContext) {
		int appid = actionContext.getAppDetail() != null ? actionContext.getAppDetail().getId() : QueryConsts.TOTAL_APPID_ID;
		requestStatsService.statsAdd(name(), appid, actionContext.getSearchId(), actionContext.getCostTime(), RestStatus.OK);

		String searchId = actionContext.getSearchId() != null ? actionContext.getSearchId() : QueryConsts.TOTAL_SEARCH_ID;
		try {
			rateLimitService.addUp(appid, searchId, 0, 0);
		} catch (Exception e) {
			logger.warn("rateLimitService.addUp exception", e);
		}
		
	}
	
	protected void preException(ActionContext actionContext, Throwable e) {
		actionContext.setResponseTime(System.currentTimeMillis());
		statLogger.error(QueryConsts.DLFLAG_PREFIX + "query_tcp_pre_exception||requestId={}||appid={}||action={}||name={}||stack={}",
				actionContext.getRequestId(), actionContext.getAppid(), actionContext.getActionName(), e.getClass().getName(), Convert.logExceptionStack(e));
		
		actionContext.setResponseTime(System.currentTimeMillis());
		traceLogger.info("_com_request_out||traceid={}||spanid={}||type=tcp||exception=pre||appid={}||requestId={}||action={}||proc_time={}||errname={}",
				actionContext.getTraceid(), actionContext.getSpanid(), actionContext.getAppid(), actionContext.getRequestId(), actionContext.getActionName(), actionContext.getCostTime(), e.getClass().getName());
		
		LogGather.recordErrorLog(e.getClass().getName() + "_" + actionContext.getAppid(), "tcp_pre_exception||appid=" + actionContext.getAppid(), e);
	}

	protected void checkFlowLimit(ActionContext actionContext) {
		if (rateLimitService.isTrafficDataOverflow(actionContext.getAppDetail().getId(), actionContext.getSearchId())) {
			throw new FlowLimitException("query flow limit, please try again!");
		}
	}
	
    public static class RetryListener<Response> implements ActionListener<Response> {
        private final ActionListener<Response> listener;
        private final ActionContext actionContext;
        private final ActionHandler actionHandler;

        private int retryTimes;

        public RetryListener(ActionHandler actionHandler, ActionContext actionContext, ActionListener<Response> listener, int retryTimes) {
        	this.actionHandler = actionHandler;
            this.listener = listener;
            this.actionContext = actionContext;
            this.retryTimes = retryTimes;
        }

        @Override
        public void onResponse(Response response) {
            listener.onResponse(response);
        }

        @Override
        public void onFailure(Throwable e) {
        	Throwable ex = ExceptionsHelper.unwrapCause(e);
            if (ex instanceof ClusterBlockException) {
            	try {
					Thread.sleep(QueryConsts.RETRY_SLEEP_MILLIS);
				} catch (Exception e1) {
            		// pass
				}

                if (retryTimes >= QueryConsts.RETRY_COUNT) {
                    listener.onFailure(e);
                } else {
                    try {
                    	actionHandler.handleInterRequest(actionContext, retryTimes+1);
                    } catch(final Exception t) {
                        // this exception can't come from the TransportService as it doesn't throw exceptions at all
                        listener.onFailure(t);
                    }
                }
            } else {
                listener.onFailure(e);
            }
        }
    }
}
