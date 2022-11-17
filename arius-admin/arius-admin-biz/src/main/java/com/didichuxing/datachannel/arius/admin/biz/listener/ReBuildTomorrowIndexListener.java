package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.event.index.ReBuildTomorrowIndexEvent;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
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
			 //保证数据已经刷到数据库，如果立即执行，会存在获取不到数据库中数据的状态，所以等待5000millis
			Thread.sleep(5000);
			boolean tomorrowIndex = templatePreCreateManager.reBuildTomorrowIndex(reBuildTomorrowIndexEvent.getLogicId(), 3);
			if (Boolean.TRUE.equals(tomorrowIndex)){
				LOGGER.info(
					"class=ReBuildTomorrowIndexListener||method=onApplicationEvent||logicId={}||error=success rebuild",
					reBuildTomorrowIndexEvent.getLogicId());
			}
		} catch (Exception e) {
			LOGGER.error(
					"class=ReBuildTomorrowIndexListener||method=onApplicationEvent||logicId={}",
					reBuildTomorrowIndexEvent.getLogicId(),e);
		}
	}
}