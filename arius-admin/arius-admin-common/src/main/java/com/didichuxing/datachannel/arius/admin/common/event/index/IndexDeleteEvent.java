package com.didichuxing.datachannel.arius.admin.common.event.index;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import java.util.List;
import org.springframework.context.ApplicationEvent;

/**
 * @author shizeying
 * @date 2022/07/18
 */

public class IndexDeleteEvent extends ApplicationEvent {
	private Integer projectId;
	private String operator;
	private List<IndexCatCellDTO> catCellList;
	
	/**
	 * @param source
	 */
	public IndexDeleteEvent(Object source, List<IndexCatCellDTO> catCellList,Integer projectId,String operator) {
		super(source);
		this.catCellList = catCellList;
		this.projectId=projectId;
		this.operator=operator;
	}
	
	public Integer getProjectId() {
		return projectId;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public List<IndexCatCellDTO> getCatCellList() {
		return catCellList;
	}
}