package com.didichuxing.datachannel.arius.admin.common.event.index;

import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import java.util.List;
import org.springframework.context.ApplicationEvent;

/**
 * @author shizeying
 * @date 2022/07/30
 */
public class RefreshCatIndexInfoEvent extends ApplicationEvent {
	
	
	 private  final List<TupleTwo</*clusterPhy*/String,/*index*/String>> clusterIndexTupleList;
	
	public RefreshCatIndexInfoEvent(Object source, List<TupleTwo<String, String>> clusterIndexTupleList) {
		super(source);
		this.clusterIndexTupleList = clusterIndexTupleList;
	}
	
	public List<TupleTwo<String, String>> getClusterIndexTupleList() {
		return clusterIndexTupleList;
	}
}