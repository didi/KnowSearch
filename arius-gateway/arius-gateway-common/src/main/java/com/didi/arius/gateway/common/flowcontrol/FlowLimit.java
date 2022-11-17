package com.didi.arius.gateway.common.flowcontrol;

import com.didi.arius.gateway.common.enums.FlowStatus;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import java.util.Random;

/**
* @author weizijun
* @date：2016年8月25日
* 
*/
public class FlowLimit {
	private ILog log = LogFactory.getLog(FlowLimit.class);

	/**
	 * //阀值，如果flowRandom产生的随机数超过该阀值，则判断为over flow，否则即判断down
	 */
	private int threshold;

	/**
	 * //阀值，如果flowRandom产生的随机数超过该阀值，则判断为over flow，否则即判断down
	 */
	private static final double UP_FACTOR = 0.3;

	/**
	 * //threshold down减少因子
	 */
	private static final double DOWN_FACTOR = 0.5;

	private static final int MAX_THRESHOLD = 2000;
	private static final Random flowRandom = new Random();

	private String areaId;

	public FlowLimit(String areaId) {
		threshold = 0;
		this.areaId = areaId;
	}
	
	public int getThreshold() {
		return threshold;
	}
	
	public boolean isOverflow() {
		
		if (threshold == 0)
			return false;
		else if (threshold >= MAX_THRESHOLD) {
			log.warn("Threshold {} larger than max {}", threshold, MAX_THRESHOLD);
			return true;
		}
		 
		else
		{
			//这么做的好处是一旦被判定为over flow，不会限制所有的该namespace的request
			//而是以一定的概率限制request
			return flowRandom.nextInt(MAX_THRESHOLD) < threshold;
		}
		 
	}
	
	public void limitLevelTouch(FlowStatus status) {
		switch (status) {
		case KEEP :
			limitLevelKeep();
			break;
		case UP :
			limitLevelUp();
			break;
		case DOWN :
			limitLevelDown();
			break;
		default:
			break;
		}
	}
	
	public void limitLevelUp() {
		//逐步提高被判断为over flow的概率
		if (threshold < MAX_THRESHOLD - 10) {
			threshold = (int)(threshold + UP_FACTOR * (MAX_THRESHOLD - threshold));
		}
		
		log.warn("flow limit up areaId {} curt {}", areaId, threshold);
	}
	
	public void limitLevelKeep() {
		// Do nothing
	}
	
	public void limitLevelDown() {
		if (threshold == 0) {
			return ;
		}
		
		//这样做的好处是一旦前期被限流，不会因为一个down而导致全被解流，它是一个逐步解流的过程
		//一旦threshold阀值低于50，则全解流
		threshold = (int)(threshold - DOWN_FACTOR * threshold);
		if (threshold < 50) {
			threshold = 0;
		}
		
		log.warn("flow limit down areaId {} curt {}", areaId, threshold);
	}
}
