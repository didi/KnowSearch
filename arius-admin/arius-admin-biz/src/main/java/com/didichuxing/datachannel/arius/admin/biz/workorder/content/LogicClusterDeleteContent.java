package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;

/**
 * Created by linyunan on 2021-06-11
 */
@Data
public class LogicClusterDeleteContent extends BaseContent {

	/**
	 * 主键
	 */
	private Long    id;

    /**
     * 集群名称
     */
    private String  name;

    /**
     * 集群类型
     */
    private Integer type;

    /**
     * 责任人
     */
    private String  responsible;
}
