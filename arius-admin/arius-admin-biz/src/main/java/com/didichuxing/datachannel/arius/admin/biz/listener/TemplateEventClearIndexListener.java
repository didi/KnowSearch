package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.event.index.IndexDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
			final Map<String, List<String>> clusterPhy2IndexListMap = ConvertUtil.list2MapOfList(event.getCatCellList(),
					IndexCatCellDTO::getCluster, IndexCatCellDTO::getIndex);
					
			for (Entry<String, List<String>> clusterPhy2IndexList : clusterPhy2IndexListMap.entrySet()) {
				indicesManager.deleteIndexByCLusterPhy(clusterPhy2IndexList.getKey(),clusterPhy2IndexList.getValue(),event.getProjectId(), event.getOperator());
			}
		} catch (Exception e) {
			String index = event.getCatCellList().stream().map(IndexCatCellDTO::getIndex)
					.collect(Collectors.joining(","));
			LOGGER.error(
					"class=TemplateEventClearIndexListener||method=onApplicationEvent||projectId={}||index={}||ErrorMsg={}",
					event.getProjectId(), index, e.getMessage());
		}
	}
}