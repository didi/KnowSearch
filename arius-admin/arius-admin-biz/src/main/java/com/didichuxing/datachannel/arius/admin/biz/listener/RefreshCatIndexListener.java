package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.event.index.RefreshCatIndexInfoEvent;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 此事件主要用于平台操作之后， arius_cat_index_info进行实时的异步写入， 从而保证平台操作是可以近实时看到，
 * 目前只包含了_forcemerge操作之后对shard和segment的更新策略
 *
 * @author shizeying
 * @date 2022/07/30
 */
@Component
public class RefreshCatIndexListener implements ApplicationListener<RefreshCatIndexInfoEvent> {
	private static final ILog LOGGER = LogFactory.getLog(RefreshCatIndexListener.class);
	
	@Autowired
	private ESIndexCatService esIndexCatService;
	
	/**
	 * @param refreshCatIndexInfoEvent
	 */
	@Override
	public void onApplicationEvent(RefreshCatIndexInfoEvent refreshCatIndexInfoEvent) {
		Map<String, Set<String>> clusterPhy2IndexSetMap = refreshCatIndexInfoEvent.getClusterIndexTupleList().stream()
				.distinct()
				.collect(Collectors.groupingBy(TupleTwo::v1,
						Collectors.mapping(TupleTwo::v2, Collectors.toSet())
				));
		
		for (Entry<String, Set<String>> clusterPhy2IndexEntry : clusterPhy2IndexSetMap.entrySet()) {
			String clusterPhy = clusterPhy2IndexEntry.getKey();
			Set<String> indexList = clusterPhy2IndexEntry.getValue();
			
			Result<List<IndexCatCellDTO>> result = esIndexCatService.syncGetSegmentsIndexList(clusterPhy, indexList);
			if (result.failed()) {
				LOGGER.warn("class={}||method=onApplicationEvent||cluster={}||msg={}", getClass().getSimpleName(),
						clusterPhy, result.getMessage());
			}
			List<IndexCatCellDTO> indexCatCellList = result.getData();
			if (CollectionUtils.isEmpty(indexCatCellList)) {
				continue;
			}
			
			try {
				esIndexCatService.syncUpsertCatIndex(indexCatCellList, 3);
			} catch (Exception e) {
				LOGGER.error("class={}||method=onApplicationEvent||cluster={}", getClass().getSimpleName(), clusterPhy,
						e);
			}
			
		}
		
	}
}