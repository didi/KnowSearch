package com.didichuxing.datachannel.arius.admin.biz.listener;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.event.index.IndexDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.EventException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

/**
 * 模板事件:删除模板之后，发布事件删除关联的列表
 *
 * @author shizeying
 * @date 2022/07/18
 */
@Component
public class TemplateEventClearIndexListener extends ApplicationRetryListener<IndexDeleteEvent> {
	private static final ILog           LOGGER = LogFactory.getLog(TemplateEventClearIndexListener.class);

	@Autowired
	private              IndicesManager indicesManager;

	@Override
	public void onApplicationRetryEvent(@NotNull IndexDeleteEvent event) throws EventException {
		final Map<String, List<String>> clusterPhy2IndexListMap = ConvertUtil.list2MapOfList(event.getCatCellList(),
				IndexCatCellDTO::getCluster, IndexCatCellDTO::getIndex);

		for (Entry<String, List<String>> clusterPhy2IndexList : clusterPhy2IndexListMap.entrySet()) {
			deleteIndexByCLusterPhy(clusterPhy2IndexList.getKey(),clusterPhy2IndexList.getValue(),event.getProjectId(), event.getOperator());
		}
	}

	private void deleteIndexByCLusterPhy(String clusterPhy, List<String> indexNameList, Integer projectId, String operator) throws EventException{
		Result<Void> result = indicesManager.deleteIndexByCLusterPhy(clusterPhy, indexNameList, projectId, operator);
		if(null == result || result.failed()){
			LOGGER.error(
					"method=deleteIndexByCLusterPhy||projectId={}||index={}||ErrorMsg={}",
					clusterPhy, indexNameList, "deleteIndexByCLusterPhy error!");
			throw new EventException("deleteIndexByCLusterPhy error!");
		}
	}
}