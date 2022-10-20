package com.didichuxing.datachannel.arius.admin.common.event.index;

import org.springframework.context.ApplicationEvent;

/**
 * @author shizeying
 * @date 2022/07/18
 */
public class ReBuildTomorrowIndexEvent extends ApplicationEvent {
	 private Integer                 logicId;
	
	/**
	 * @param source
	 */
	public ReBuildTomorrowIndexEvent(Object source,Integer logicId) {
		super(source);
		this.logicId=logicId;
	}
	
	public Integer getLogicId() {
		return logicId;
	}
}