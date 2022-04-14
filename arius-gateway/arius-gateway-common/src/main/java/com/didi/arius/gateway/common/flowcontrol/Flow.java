package com.didi.arius.gateway.common.flowcontrol;

import com.didi.arius.gateway.common.enums.FlowStatus;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class Flow {
	/**
	 * 当前大小
	 */
	private AtomicInteger curtQuantity = new AtomicInteger(0);

	/**
	 * 每秒钟的流控值，通过后台线程计算所得
	 */
	private AtomicInteger lastPerSecond = new AtomicInteger(0);

	/**
	 * 上一次计算流控值的时间
	 */
	private AtomicLong lastCalTime = new AtomicLong(0);

	/**
	 * 上限
	 */
	private AtomicInteger lowerBound = new AtomicInteger(0);

	/**
	 * 下限
	 */
	private AtomicInteger upperBound = new AtomicInteger(0);

	/**
	 * 当前状态
	 */
	private FlowStatus status = FlowStatus.DOWN;
	
	public int getCurtQuantity() {
		return curtQuantity.get();
	}
	
	public int addCurtQuantity(int size) {
		return this.curtQuantity.addAndGet(size);
	}
	
	public int subCurtQuantity(int size) {
		return this.curtQuantity.addAndGet(-size);
	}
	
	public int getLastPerSecond() {
		return lastPerSecond.get();
	}
	
	public void setLastPerSecond(int lastPerSecond) {
		this.lastPerSecond.set(lastPerSecond);
	}

	public long getLastCalTime() {
		return lastCalTime.get();
	}
	
	public void setLastCalTime(long lastCalTime) {
		this.lastCalTime.set(lastCalTime);
	}

	public int getLowerBound() {
		return lowerBound.get();
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound.set(lowerBound);
	}

	public int getUpperBound() {
		return upperBound.get();
	}

	public void setUpperBound(int upperBound) {
		this.upperBound.set(upperBound);
	}

	public FlowStatus getStatus() {
		return status;
	}

	public void setStatus(FlowStatus status) {
		this.status = status;
	}

}
