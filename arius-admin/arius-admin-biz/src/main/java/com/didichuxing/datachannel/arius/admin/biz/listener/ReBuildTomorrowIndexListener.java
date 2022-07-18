package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.event.index.ReBuildTomorrowIndexEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ReBuildTomorrowIndexListener implements ApplicationListener<ReBuildTomorrowIndexEvent> {
	private static final ILog             LOGGER = LogFactory.getLog(ClusterLogicChangeListener.class);
	@Autowired
	private              PreCreateManager templatePreCreateManager;
	
	/**
	 * @param reBuildTomorrowIndexEvent
	 */
	@Override
	public void onApplicationEvent(ReBuildTomorrowIndexEvent reBuildTomorrowIndexEvent) {
		try {
			boolean tomorrowIndex = templatePreCreateManager.reBuildTomorrowIndex(reBuildTomorrowIndexEvent.getLogicId(), 3);
			if (Boolean.TRUE.equals(tomorrowIndex)){
				LOGGER.info(
					"class=ReBuildTomorrowIndexListener||method=onApplicationEvent||logicId={}||error=success rebuild",
					reBuildTomorrowIndexEvent.getLogicId());
			}
		} catch (ESOperateException e) {
			LOGGER.error(
					"class=ReBuildTomorrowIndexListener||method=onApplicationEvent||logicId={}",
					reBuildTomorrowIndexEvent.getLogicId(),e);
		}
	}
}