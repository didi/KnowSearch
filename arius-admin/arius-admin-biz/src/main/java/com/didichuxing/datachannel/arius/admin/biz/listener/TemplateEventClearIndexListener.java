package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.event.index.IndexDeleteEvent;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 模板事件:删除模板之后，发布事件删除关联的列表
 *
 * @author shizeying
 * @date 2022/07/18
 */
@Component
public class TemplateEventClearIndexListener implements ApplicationListener<IndexDeleteEvent> {
	private static final ILog           LOGGER = LogFactory.getLog(TemplateEventClearIndexListener.class);
	@Autowired
	private              IndicesManager indicesManager;
	
	/**
	 * @param event
	 */
	@Override
	public void onApplicationEvent(@NotNull IndexDeleteEvent event) {
		try {
			for (IndexCatCellDTO indexCatCellDTO : event.getCatCellList()) {
				if (!AuthConstant.SUPER_PROJECT_ID.equals( event.getProjectId()) && StringUtils.isNotBlank(indexCatCellDTO.getClusterLogic())){
					indexCatCellDTO.setCluster(indexCatCellDTO.getClusterLogic());
				}
			}
			indicesManager.deleteIndex(event.getCatCellList(), event.getProjectId(), event.getOperator());
		} catch (Exception e) {
			String index = event.getCatCellList().stream().map(IndexCatCellDTO::getIndex)
					.collect(Collectors.joining(","));
			LOGGER.error(
					"class=TemplateEventClearIndexListener||method=onApplicationEvent||projectId={}||index={}||ErrorMsg={}",
					event.getProjectId(), index, e.getMessage());
		}
	}
}