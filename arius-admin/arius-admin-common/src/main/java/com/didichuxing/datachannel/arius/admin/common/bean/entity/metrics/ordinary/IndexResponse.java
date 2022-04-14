package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexResponse implements Serializable {

	/**
	 * 索引名称
	 */
    private String index;

	/**
	 * 文档数
	 */
	private long   dc;
}
