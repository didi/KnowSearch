package com.didi.arius.gateway.common.flowcontrol;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.FlowStatus;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class FlowController {
	private static final ILog logger = LogFactory.getLog(FlowController.class);
	
	private int appid;

	/**
	 * 计算流控的时间间隔
	 */
	private int calIntervalSecond;

	ConcurrentMap<String, AreaFlow> flowLimitMap = new ConcurrentHashMap<>();
	
	public FlowController(int appid, int calIntervalSecond) {
		this.appid = appid;
		this.calIntervalSecond = calIntervalSecond;
	}
	
	public boolean addUpFlow(Flow flow, int size) {
		if (flow.getCurtQuantity() + size < 0) {
			flow.setStatus(FlowStatus.UP);
		} else {
			int curt = flow.addCurtQuantity(size);
			if (curt < 0) {
				flow.subCurtQuantity(size);
				flow.setStatus(FlowStatus.UP);
			} else if ((curt / calIntervalSecond) >= flow.getUpperBound()) {
				flow.setStatus(FlowStatus.UP);
			}
		}
		
		return flow.getStatus() == FlowStatus.DOWN;
	}
	
	public boolean addUpIn(String searchId, int in) {
		boolean isRelaxed = false;
		if (!searchId.equals(QueryConsts.TOTAL_SEARCH_ID)) {
			isRelaxed = addUpIn(QueryConsts.TOTAL_SEARCH_ID, in);
		}
		
		AreaFlow areaFlow = getAreaFlow(searchId);
		if (areaFlow == null) {
			return true;
		}
		
		boolean limit = false;
		limit = !addUpFlow(areaFlow.getIn(), in) || limit;
		limit = !addUpFlow(areaFlow.getOps(), 1) || limit;
		if (limit) {
			areaFlow.setStatus(FlowStatus.UP);
			logger.info("appid={} Add up request: search_id={}, in={}, status={}",
					appid, searchId, in, areaFlow.getStatus());
		}
		
		return isRelaxed && areaFlow.getStatus() == FlowStatus.DOWN;
	}
	
	public boolean addUpOut(String searchId, int out) {
		boolean isRelaxed = false;
		if (!searchId.equals(QueryConsts.TOTAL_SEARCH_ID)) {
			isRelaxed = addUpOut(QueryConsts.TOTAL_SEARCH_ID, out);
		}
		
		AreaFlow areaFlow = getAreaFlow(searchId);
		if (areaFlow == null) {
			return true;
		}
		
		boolean limit = false;
		limit = !addUpFlow(areaFlow.getOut(), out) || limit;
		if (limit) {
			areaFlow.setStatus(FlowStatus.UP);
			logger.info("appid={} Add up response: search_id={}, out={}, status={}",
					appid, searchId, out, areaFlow.getStatus());
		}
		
		return isRelaxed && areaFlow.getStatus() == FlowStatus.DOWN;
	}
	
	public void backgroundCalFlows() {
		for (Map.Entry<String, AreaFlow> entry : flowLimitMap.entrySet()) {
			AreaFlow areaFlow = entry.getValue();
			FlowStatus statusIn = calCurrentFlow(areaFlow.getIn());
			if (statusIn != FlowStatus.DOWN) {
				logger.info("Flow IN Limit: appid={}, searchId={}, status={}, value={}", appid, entry.getKey(), statusIn, areaFlow.getIn().getLastPerSecond());
			}
			
			FlowStatus statusOut = calCurrentFlow(areaFlow.getOut());
			if (statusOut != FlowStatus.DOWN) {
				logger.info("Flow OUT Limit: appid={}, searchId={}, status={}, value={}", appid, entry.getKey(), statusOut, areaFlow.getOut().getLastPerSecond());
			}
			
			FlowStatus statusOps = calCurrentFlow(areaFlow.getOps());
			if (statusOps != FlowStatus.DOWN) {
				logger.info("Flow OPS Limit: appid={}, searchId={}, status={}, value={}", appid, entry.getKey(), statusOps, areaFlow.getOps().getLastPerSecond());
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Flow rate: appid={}, searchId={}, in={} {}, out={} {}, ops={} {}",
						 appid,entry.getKey(), areaFlow.getIn().getLastPerSecond(), statusIn,
						areaFlow.getOut().getLastPerSecond(), statusOut,
						areaFlow.getOps().getLastPerSecond(), statusOps);
			}
			
			if (statusIn == FlowStatus.UP
					|| statusOut == FlowStatus.UP
					|| statusOps == FlowStatus.UP) {
				areaFlow.setStatus(FlowStatus.UP);
			} else if (statusIn == FlowStatus.KEEP
					|| statusOut == FlowStatus.KEEP
					|| statusOps == FlowStatus.KEEP) {
				areaFlow.setStatus(FlowStatus.KEEP);
			} else {
				areaFlow.setStatus(FlowStatus.DOWN);
			}
		}
	}
	
	private FlowStatus calCurrentFlow(Flow flow) {
		long now = System.currentTimeMillis();
		int quantity = flow.getCurtQuantity();
		long lastCalTime = flow.getLastCalTime();
		int spend = (int) (lastCalTime > 0 ? now - lastCalTime : calIntervalSecond * 1000);
		int lastPerSecond = spend <  1000.0001 ? quantity : (int) ((double) quantity / spend * 1000);
		flow.setLastPerSecond(lastPerSecond);
		
		flow.setLastCalTime(now);
		
		flow.subCurtQuantity(quantity);
		
		if (lastPerSecond > flow.getUpperBound()) {
			flow.setStatus(FlowStatus.UP);
		} else if (flow.getStatus() == FlowStatus.UP && lastPerSecond > flow.getLowerBound()) {
			flow.setStatus(FlowStatus.KEEP);
		} else {
			flow.setStatus(FlowStatus.DOWN);
		}
		
		return flow.getStatus();
	}
	
	private AreaFlow getAreaFlow(String searchId) {
		if (!flowLimitMap.containsKey(searchId)) {
			synchronized (flowLimitMap) {
				if (!flowLimitMap.containsKey(searchId)) {
					String areaId = formAreaId(appid, searchId);
					AreaFlow areaFlow = AreaFlowCache.getInstance().getAreaFlow(areaId);
					if (areaFlow == null) {
						return null;
					}
					flowLimitMap.put(searchId, areaFlow);
				}
			}
		}
		
		return flowLimitMap.get(searchId);
	}
	
	public static String formAreaId(int appid, String searchId) {
		StringBuilder buffer = new StringBuilder(String.valueOf(appid));
		buffer.append("_");
		buffer.append(searchId);
		return buffer.toString();
	}
	
	public int getAppid() {
		return appid;
	}
	
	public Map<String, AreaFlow> getFlowLimitMap() {
		return flowLimitMap;
	}
}
