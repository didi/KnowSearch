package com.didi.arius.gateway.core.es.tcp;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.event.ActionPostResponseEvent;
import com.didi.arius.gateway.common.metadata.ActionContext;
import com.didi.arius.gateway.core.component.SpringTool;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.log.LogGather;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年9月20日
* 
*/
public class ActionListenerImpl<T extends ActionResponse> implements ActionListener<T> {
	protected static final ILog logger = LogFactory.getLog(ActionListenerImpl.class);
	protected static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);
	protected static final ILog traceLogger = LogFactory.getLog(QueryConsts.TRACE_LOGGER);
	
	protected ActionContext actionContext;
	
	public ActionListenerImpl(ActionContext actionContext) {
		this.actionContext = actionContext;
	}
	
	@Override
	public void onResponse(T response) {
		try {
			actionContext.getSemaphore().release();
			
			actionContext.getChannel().sendResponse(response);
			
			postResponse(actionContext);
		} catch (IOException e) {
			logger.error("onResponse sendResponse error:", e);
		}
	}

	@Override
	public void onFailure(Throwable e) {
		try {
			actionContext.getSemaphore().release();
			
			actionContext.getChannel().sendResponse(e);
			
			postResponse(actionContext, e);
		} catch (IOException e1) {
			logger.error("onFailure sendResponse error:", e1);
		}
	}
	
	protected void postResponse(ActionContext actionContext, Throwable e) {
		actionContext.setResponseTime(System.currentTimeMillis());
		statLogger.error(QueryConsts.DLFLAG_PREFIX + "query_tcp_exception||requestId={}||appid={}||action={}||name={}", 
				actionContext.getRequestId(), actionContext.getAppid(), actionContext.getActionName(), e.getClass().getName());
		
		traceLogger.info("_com_request_out||traceid={}||spanid={}||type=tcp||exception=after||appid={}||requestId={}||action={}||proc_time={}||errname={}",
				actionContext.getTraceid(), actionContext.getSpanid(), actionContext.getAppid(), actionContext.getRequestId(), actionContext.getActionName(), actionContext.getCostTime(), e.getClass().getName());
		
		LogGather.recordErrorLog(e.getClass().getName() + "_" + actionContext.getAppid(), "tcp_exception||appid=" + actionContext.getAppid(), e);

		SpringTool.publish(new ActionPostResponseEvent(this, actionContext));
	}	
	
	protected void postResponse(ActionContext actionContext) {
		actionContext.setResponseTime(System.currentTimeMillis());
		statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_tcp_response||requestId={}||appid={}||action={}||cost={}", 
				actionContext.getRequestId(), actionContext.getAppid(), actionContext.getActionName(), actionContext.getCostTime());

		traceLogger.info("_com_request_out||traceid={}||spanid={}||type=tcp||appid={}||requestId={}||action={}||proc_time={}",
				actionContext.getTraceid(), actionContext.getSpanid(), actionContext.getAppid(), actionContext.getRequestId(), actionContext.getActionName(), actionContext.getCostTime());

		SpringTool.publish(new ActionPostResponseEvent(this, actionContext));
	}
	
}
